/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package main

/*
#include <stdlib.h>
// ots的数据结构
typedef struct DataItem{
	char * k; //key
	int ksize;//ksize
	char * v; //vaLue
	int vsize;//vsize
	int p; //partition
	char * si; //splitindex
	struct DataItem *next; //next
}DataItem;
struct DataGroup{
	struct DataItem *data; //data
	struct DataGroup *next; //next
};
struct CotsPK{
	char * name;
	int partition;
	char * k;
	int ksize;
	char * splitIndex;
};
struct CgetNext{
	struct CotsPK *startPK;
	struct CotsPK *endPK;
	struct CgetNext *next;
};
struct CotsItem{
	char * k; //key
	int ksize;//ksize
	char * v; //vaLue
	int vsize;//vsize
	char * splitIndex; //splitindex
	struct CotsItem *next; //next
};
struct CotsPartitionsOnePage{
	struct CotsItem *data; //getRange 返回的一批数据
	struct CgetNext *getNext; //表示该 Page 是否有下一个 Page
	struct CotsPartitionsOnePage *next; //指下一个 partition 的 Page
};

static void free_point(char *p){
   free(p);
}
static void free_dataitem(DataItem *p){
   free(p);
}
*/
import "C"

import (
	"encoding/json"
	"fmt"
	"github.com/aliyun/aliyun-tablestore-go-sdk/tablestore"
	"github.com/wefe/go-utils/fc"
	"math"
	"strconv"
	"sync"
	"time"
	"unsafe"
)

const (
	TableKeyName        = "k"
	TableName           = "name"
	TablePartitionName  = "partition"
	TableSplitIndexName = "split_index"
	TableValueName      = "v"
)

//===================== free ========================
//export freeCharP
/*
释放char指针的内存
*/
func freeCharP(p *C.char) {
	C.free(unsafe.Pointer(p))
}

func freeDataItem(dataItem *C.struct_DataItem) {
	tail := dataItem

	// 遍历链表清理
	for {
		if tail == nil {
			break
		}
		if tail.k != nil {
			C.free(unsafe.Pointer(tail.k))
		}
		if tail.v != nil {
			C.free(unsafe.Pointer(tail.v))
		}
		if tail.si != nil {
			C.free(unsafe.Pointer(tail.si))
		}
		next := tail.next
		if tail != nil {
			C.free(unsafe.Pointer(tail))
		}
		tail = next
	}
}

func freeDataGroup(datagroup *C.struct_DataGroup) {

	for {
		if datagroup == nil || datagroup.data == nil {
			break
		}
		next := datagroup.next
		// 清理dataItem
		freeDataItem(datagroup.data)
		// 清理当前dataGroup的指针
		C.free(unsafe.Pointer(datagroup))

		datagroup = next
	}

}

func freeCotsItem(otsItem *C.struct_CotsItem) {
	for {
		if otsItem == nil {
			break
		}
		if otsItem.k != nil {
			C.free(unsafe.Pointer(otsItem.k))
		}
		if otsItem.v != nil {
			C.free(unsafe.Pointer(otsItem.v))
		}
		if otsItem.splitIndex != nil {
			C.free(unsafe.Pointer(otsItem.splitIndex))
		}
		next := otsItem.next
		C.free(unsafe.Pointer(otsItem))
		otsItem = next
	}
}

func freeCotsPK(otsPK *C.struct_CotsPK) {
	if otsPK != nil {
		if otsPK.name != nil {
			C.free(unsafe.Pointer(otsPK.name))
		}
		if otsPK.k != nil {
			C.free(unsafe.Pointer(otsPK.k))
		}
		if otsPK.splitIndex != nil {
			C.free(unsafe.Pointer(otsPK.splitIndex))
		}
		C.free(unsafe.Pointer(otsPK))
	}
}

func freeCgetNext(getNext *C.struct_CgetNext) {
	tail := getNext
	for {
		if tail == nil {
			break
		}
		next := tail.next
		freeCotsPK(tail.startPK)
		freeCotsPK(tail.endPK)
		tail = next
	}
}

//export freeCotsPartitionPage
func freeCotsPartitionPage(page *C.struct_CotsPartitionsOnePage) {
	for {
		if page == nil {
			break
		}
		next := page.next
		freeCotsItem(page.data)
		freeCgetNext(page.getNext)
		C.free(unsafe.Pointer(page))
		page = next
	}
}

//===================== OTS ========================
//export getPKNameGo
func getPKNameGo(name *C.char, partition int) *C.char {
	pkName := getPKName(C.GoString(name), partition)
	return C.CString(pkName)
}
func getPKName(name string, partition int) string {
	_hash_code := math.Abs(float64(hash_code(strconv.Itoa(partition) + name)))
	_code := int(int32(_hash_code) % 10007)
	//fmt.Println(_code)
	return strconv.Itoa(_code) + "_" + name
}

func hash_code(s string) int32 {
	var h int32 = 0
	if len(s) > 0 {
		for item := range s {
			h = 31*h + int32(s[item])
		}
		return h
	} else {
		return 0
	}
}

/**
写入每批数据
*/
func dealItemGroup(client *tablestore.TableStoreClient, namespace string, name string, item C.struct_DataItem) {

	//fmt.Println("begin batch write")
	batchWriteReq := &tablestore.BatchWriteRowRequest{}
	count := 0
	for {

		putRow := new(tablestore.PutRowChange)
		putRow.TableName = namespace

		//fmt.Println(dataItem.Key)
		pk := new(tablestore.PrimaryKey)
		pk.AddPrimaryKeyColumn(TableName, getPKName(name, int(item.p)))
		pk.AddPrimaryKeyColumn(TablePartitionName, int64(item.p))
		pk.AddPrimaryKeyColumn(TableKeyName, C.GoBytes(unsafe.Pointer(item.k), item.ksize))
		pk.AddPrimaryKeyColumn(TableSplitIndexName, C.GoString(item.si))
		putRow.PrimaryKey = pk

		//fmt.Println(pk)
		putRow.AddColumn(TableValueName, C.GoBytes(unsafe.Pointer(item.v), item.vsize))
		putRow.SetCondition(tablestore.RowExistenceExpectation_IGNORE)
		batchWriteReq.AddRowChange(putRow)
		//batchWriteReq.IsAtomic = true
		count = count + 1

		if item.next == nil {
			break
		}
		item = *item.next
	}

	// 最大重试3次
	for retry := 0; retry < 10; retry++ {
		response, err := client.BatchWriteRow(batchWriteReq)
		if err != nil {
			fmt.Println("batch write request failed with:", err, response, "retry index:", retry)
			continue
		} else {
			isAllSuccess := true
			for _, rows := range response.TableToRowsResult {
				for _, row := range rows {
					if !row.IsSucceed {
						isAllSuccess = false
						break
					}
				}
			}
			if isAllSuccess {
				fmt.Println("batch write row finished, all success,", response.ResponseInfo.RequestId, "retry index:", retry)
				break
			} else {
				fmt.Println("batch write row finished, fail,", response.TableToRowsResult, response.ResponseInfo.RequestId, "retry index:", retry)
				time.Sleep(time.Millisecond * 200)
			}
		}
	}
	fmt.Println("每批数据：", count)
}

/**
批量写入数据到OTS
*/
//export putAllBatchGroup
func putAllBatchGroup(endpoint *C.char, instanceName *C.char, accessKeyId *C.char, accessKeySecret *C.char,
	namespace *C.char, name *C.char, datagroup C.struct_DataGroup) *C.char {

	httpTimeout := &tablestore.HTTPTimeout{
		ConnectionTimeout: time.Second * 60,
		RequestTimeout:    time.Second * 60}
	config := &tablestore.TableStoreConfig{
		RetryTimes:         10,
		HTTPTimeout:        *httpTimeout,
		MaxRetryTime:       time.Second * 60,
		MaxIdleConnections: 3000}
	client := tablestore.NewClientWithConfig(C.GoString(endpoint), C.GoString(instanceName),
		C.GoString(accessKeyId), C.GoString(accessKeySecret), "", config)

	namespaceStr := C.GoString(namespace)
	nameStr := C.GoString(name)

	var wg sync.WaitGroup
	startTime := time.Now()

	for {
		if datagroup.data == nil {
			break
		}

		wg.Add(1)
		go func(item *C.struct_DataItem) {
			dealItemGroup(client, namespaceStr, nameStr, *item)
			defer wg.Done()
		}(datagroup.data)

		// deal next
		if datagroup.next == nil {
			break
		}
		datagroup = *datagroup.next

	}

	wg.Wait()
	consumeTime := time.Since(startTime)
	fmt.Println("run time:", consumeTime)

	return C.CString("ok")
}

// 获取 CgetNext
func _getCgetNext(PK *tablestore.PrimaryKey, getNext *C.struct_CgetNext) *C.struct_CgetNext {
	if PK == nil {
		return nil
	}

	newgetNext := (*C.struct_CgetNext)(C.malloc(C.size_t(unsafe.Sizeof(C.struct_CgetNext{}))))
	newgetNext.next = nil

	// 实例新的 start CotsPK
	_CotsPK := (*C.struct_CotsPK)(C.malloc(C.size_t(unsafe.Sizeof(C.struct_CotsPK{}))))
	nextStartPK := PK.PrimaryKeys
	name := nextStartPK[0].Value
	partition := nextStartPK[1].Value.(int64)
	nextK := nextStartPK[2].Value.([]byte)

	k := C.CBytes(nextK) //unsafe.Pointer
	ksize := len(nextK)
	splitIndex := nextStartPK[3].Value

	_CotsPK.name = C.CString(name.(string))
	_CotsPK.partition = C.int(partition)
	_CotsPK.k = (*C.char)(k)
	_CotsPK.ksize = C.int(ksize)
	_CotsPK.splitIndex = C.CString(splitIndex.(string))
	newgetNext.startPK = _CotsPK

	_CotsEndPK := (*C.struct_CotsPK)(C.malloc(C.size_t(unsafe.Sizeof(C.struct_CotsPK{}))))

	endPK := getNext.endPK
	_CotsEndPK.name = C.CString(C.GoString(endPK.name))
	_CotsEndPK.partition = C.int(endPK.partition)

	if endPK.k == nil {
		_CotsEndPK.k = nil
	} else {
		_CotsEndPK.k = (*C.char)(C.CBytes(C.GoBytes(unsafe.Pointer(endPK.k), C.int(endPK.ksize))))
	}

	_CotsEndPK.ksize = C.int(endPK.ksize)
	if endPK.splitIndex == nil {
		_CotsEndPK.splitIndex = nil
	} else {
		_CotsEndPK.splitIndex = C.CString(C.GoString(endPK.splitIndex))
	}

	newgetNext.endPK = _CotsEndPK

	//fmt.Println("newgetNext start k", C.GoBytes(unsafe.Pointer(newgetNext.startPK.k), C.int(ksize)))
	return newgetNext
}

//export getPartitionsOnePage
// 并发获取分片分页
func getPartitionsOnePage(getNext *C.struct_CgetNext, endpoint *C.char, instanceName *C.char, accessKeyId *C.char, accessKeySecret *C.char,
	namespace *C.char, pageLimit int32, stsToken *C.char) *C.struct_CotsPartitionsOnePage {

	//返回的数据，都需要用C的内存分配方式，否则被gc而引起异常
	head := (*C.struct_CotsPartitionsOnePage)(C.malloc(C.size_t(unsafe.Sizeof(C.struct_CotsPartitionsOnePage{}))))
	tail := head
	lastTail := tail

	var wg sync.WaitGroup
	var lock sync.Mutex // 互斥锁
	startTime := time.Now()
	//
	//client := tablestore.NewClient(C.GoString(endpoint), C.GoString(instanceName),
	//	C.GoString(accessKeyId), C.GoString(accessKeySecret))

	httpTimeout := &tablestore.HTTPTimeout{
		ConnectionTimeout: time.Second * 60,
		RequestTimeout:    time.Second * 60}
	config := &tablestore.TableStoreConfig{
		RetryTimes:         10,
		HTTPTimeout:        *httpTimeout,
		MaxRetryTime:       time.Second * 60,
		MaxIdleConnections: 3000}
	client := tablestore.NewClientWithConfig(C.GoString(endpoint), C.GoString(instanceName),
		C.GoString(accessKeyId), C.GoString(accessKeySecret), C.GoString(stsToken), config)

	for {
		if getNext == nil {
			break
		}

		wg.Add(1)

		go func(getNext *C.struct_CgetNext) {
			defer wg.Done()

			getRangeRequest := &tablestore.GetRangeRequest{}
			rangeRowQueryCriteria := &tablestore.RangeRowQueryCriteria{}
			rangeRowQueryCriteria.TableName = C.GoString(namespace)

			startPK := (*getNext).startPK
			endPK := (*getNext).endPK

			// 组装 startPK
			_startPK := new(tablestore.PrimaryKey)
			_startPK.AddPrimaryKeyColumn(TableName, C.GoString(startPK.name))
			_startPK.AddPrimaryKeyColumn(TablePartitionName, int64(startPK.partition))

			if startPK.k == nil {
				_startPK.AddPrimaryKeyColumnWithMinValue(TableKeyName)
			} else {
				fmt.Println("startPK.k", C.GoBytes(unsafe.Pointer(startPK.k), C.int(startPK.ksize)))
				_startPK.AddPrimaryKeyColumn(TableKeyName, C.GoBytes(unsafe.Pointer(startPK.k), C.int(startPK.ksize)))
			}

			if startPK.splitIndex == nil {
				_startPK.AddPrimaryKeyColumnWithMinValue(TableSplitIndexName)
			} else {
				fmt.Println("split_Index", C.GoString(startPK.splitIndex))
				_startPK.AddPrimaryKeyColumn(TableSplitIndexName, C.GoString(startPK.splitIndex))
			}

			// 组装 endPK
			_endPK := new(tablestore.PrimaryKey)
			_endPK.AddPrimaryKeyColumn(TableName, C.GoString(endPK.name))
			_endPK.AddPrimaryKeyColumn(TablePartitionName, int64(endPK.partition))

			if endPK.k == nil {
				_endPK.AddPrimaryKeyColumnWithMaxValue(TableKeyName)
			} else {
				_endPK.AddPrimaryKeyColumn(TableKeyName, C.GoBytes(unsafe.Pointer(endPK.k), C.int(endPK.ksize)))
			}

			if endPK.splitIndex == nil {
				_endPK.AddPrimaryKeyColumnWithMaxValue(TableSplitIndexName)
			} else {
				_endPK.AddPrimaryKeyColumn(TableSplitIndexName, C.GoString(endPK.splitIndex))
			}

			rangeRowQueryCriteria.StartPrimaryKey = _startPK
			rangeRowQueryCriteria.EndPrimaryKey = _endPK
			rangeRowQueryCriteria.Direction = tablestore.FORWARD
			rangeRowQueryCriteria.MaxVersion = 1
			if pageLimit > 0 {
				rangeRowQueryCriteria.Limit = pageLimit
			}

			//rangeRowQueryCriteria.Limit = 2
			getRangeRequest.RangeRowQueryCriteria = rangeRowQueryCriteria

			// todo 异常处理
			//getRangeResp, err := client.GetRange(getRangeRequest)
			for retry := 0; retry < 3; retry++ {

				getRangeResp, err := client.GetRange(getRangeRequest)
				if err != nil {
					fmt.Println("getRange_err：", err, getRangeResp.RequestId, "get range retry index:", retry)
					continue
				}
				rows := getRangeResp.Rows

				//fmt.Println("getRange 数量：", len(rows))

				// 组装 CotsItem 链表
				cOtsItemPointer := dealCotsItem(rows) // 数据链表的指针
				// 获取 CgetNext
				newGetNext := _getCgetNext(getRangeResp.NextStartPrimaryKey, getNext)

				lock.Lock()
				(*tail).data = cOtsItemPointer
				(*tail).getNext = newGetNext
				n := (*C.struct_CotsPartitionsOnePage)(C.malloc(C.size_t(unsafe.Sizeof(C.struct_CotsPartitionsOnePage{}))))
				(*tail).next = n
				lastTail = tail
				tail = (*tail).next
				lock.Unlock()

				break
			}

		}(getNext)
		getNext = getNext.next
	}

	wg.Wait()
	consumeTime := time.Since(startTime)
	fmt.Println("getPartitionsOnePageSync run time:", consumeTime)

	//(*tail).next = nil
	//(*tail).data = nil
	//(*tail).getNext = nil

	C.free(unsafe.Pointer(tail))
	//fmt.Println("tail",tail)
	tail = nil
	lastTail.next = nil
	return head
}

func dealCotsItem(rows []*tablestore.Row) *C.struct_CotsItem {

	if len(rows) == 0 {
		return nil
	}
	head := (*C.struct_CotsItem)(C.malloc(C.size_t(unsafe.Sizeof(C.struct_CotsItem{}))))
	tail := head

	for i := 0; i < len(rows); i++ {
		row := rows[i]
		// 处理 PrimaryKey
		PK := row.PrimaryKey
		pks := PK.PrimaryKeys

		k := C.CBytes(pks[2].Value.([]byte)) //unsafe.Pointer
		ksize := len(pks[2].Value.([]byte))
		splitIndex := pks[3].Value //unsafe.Pointer
		// 一列 [v]
		Col := row.Columns
		v := C.CBytes(Col[0].Value.([]byte)) //unsafe.Pointer
		vsize := len(Col[0].Value.([]byte))

		(*tail).k = (*C.char)(k)
		(*tail).ksize = C.int(ksize)
		(*tail).v = (*C.char)(v)
		(*tail).vsize = C.int(vsize)
		(*tail).splitIndex = C.CString(splitIndex.(string))

		if i == len(rows)-1 {
			(*tail).next = nil
		} else {
			n := (*C.struct_CotsItem)(C.malloc(C.size_t(unsafe.Sizeof(C.struct_CotsItem{}))))
			(*tail).next = n
			tail = (*tail).next
		}

	}
	return head
}

//======================== FC ======================
//export callFC
func callFC(inputParam *C.char, endpoint *C.char, accessKeyId *C.char, accessKeySecret *C.char,
	serviceName *C.char, functionName *C.char, qualifier *C.char) *C.char {

	resp := fc.CallFC(
		C.GoString(inputParam), C.GoString(endpoint),
		C.GoString(accessKeyId), C.GoString(accessKeySecret),
		C.GoString(serviceName), C.GoString(functionName), C.GoString(qualifier))
	data, err := json.Marshal(resp)
	if err != nil {
		panic(err)
	}
	return C.CString(string(data))
}

//======================  Test =======================
func goTest() {
	endPoint := C.CString("https://fc-***.cn-shenzhen.ots.aliyuncs.com")
	fmt.Println(C.GoString(endPoint))
}

//export memoryFreeTest
func memoryFreeTest() *C.struct_DataItem {
	// DataItem
	head := (*C.struct_DataItem)(C.malloc(C.size_t(unsafe.Sizeof(C.struct_DataItem{}))))
	tail := head

	srcData := "1234567890123456"
	data := ""
	for j := 0; j < 100*100; j++ {
		data = data + srcData
	}

	for i := 0; i < 100; i++ {
		//k := C.CString(data)
		//v := C.CString(data)
		//si := C.CString(data)
		tail.k = C.CString(data)
		tail.ksize = C.int(len(data))
		tail.v = C.CString(data + "1")
		tail.vsize = C.int(len(data))
		tail.p = 1
		tail.si = C.CString(data + "2")

		//fmt.Println(data)

		next := (*C.struct_DataItem)(C.malloc(C.size_t(unsafe.Sizeof(C.struct_DataItem{}))))
		tail.next = next
		tail = next

	}
	tail.next = nil
	tail.k = nil
	tail.v = nil
	tail.si = nil
	tail.ksize = C.int(0)
	tail.vsize = C.int(0)

	//defer C.free(unsafe.Pointer(head))
	//data = ""
	return head
}

//export freePoint
func freePoint(dataItem *C.struct_DataItem) {
	fmt.Println("todo free")
	tail := dataItem
	//var p *C.struct_DataItem

	// 遍历链表清理
	for {
		if tail == nil {
			break
		}

		fmt.Println("one free")
		func() {
			if tail.k != nil {
				C.free(unsafe.Pointer(tail.k))
			}
			if tail.v != nil {
				fmt.Println("to clean v", tail.v)
				C.free(unsafe.Pointer(tail.v))
			}
			if tail.si != nil {
				C.free(unsafe.Pointer(tail.si))
			}
		}()

		fmt.Println("k,v,si", tail.k, tail.v, tail.si)

		p := tail.next
		if tail != nil {
			C.free_dataitem(tail)
		}
		fmt.Println("tail", tail)
		tail = p

		fmt.Println("one free ok")

	}

	//return C.CString("ok")
	//C.free(unsafe.Pointer(dataItem))
	//dataItem = nil
}

func main() {
}
