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

package fc

import (
	"encoding/json"
	"fmt"
	"github.com/aliyun/fc-go-sdk"
	"github.com/tidwall/gjson"
	"github.com/wefe/go-utils/common"
	"net/http"
	"strconv"
	"sync"
)

const (
	ApiVersion = "2016-08-15"
	ErrCode    = 500
)

type fcResp struct {
	Code    int    `json:"code"`
	Message string `json:"message"`
}

/**
call single function
*/
func callSingleFC(client *fc.Client, serviceName string, functionName string, inputParam string, qualifier string) string {
	input := fc.NewInvokeFunctionInput(serviceName, functionName).
		WithPayload(common.Str2bytes(inputParam)).
		WithQualifier(qualifier)
	output, err := client.InvokeFunction(input)
	if err != nil {
		fmt.Println(err)
		// 构造一个统一结构的返回信息
		errResp := &fcResp{ErrCode, err.Error()}
		jsonResp, _ := json.Marshal(errResp)
		return string(jsonResp)
	}
	return common.Bytes2str(output.Payload)

}

func CallFC(inputParam string, endpoint string, accessKeyId string, accessKeySecret string, serviceName string, functionName string, qualifier string) []string {

	var response []string
	// fc client
	client, _ := fc.NewClient(endpoint, ApiVersion, accessKeyId, accessKeySecret,
		fc.WithTimeout(1230), fc.WithTransport(&http.Transport{MaxIdleConnsPerHost: 300}))

	// 解析参数
	singleCall := gjson.Get(inputParam, "single_call").Bool()

	if singleCall {
		response = append(response, callSingleFC(client, serviceName, functionName, inputParam, qualifier))

	} else {
		partitions := int(gjson.Get(inputParam, "source.partitions").Int())
		//fmt.Println(gjson.Get(inputParam, "source.partitions").Int())

		// 加锁方案实现接受函数响应
		var wg sync.WaitGroup
		var lock sync.Mutex //互斥锁
		//startTime := time.Now()
		for i := 0; i < partitions; i++ {
			wg.Add(1)
			go func(eachInput string, partition int) {
				// 添加分片号
				newInput := eachInput[:len(eachInput)-1] + `,"partition":` + strconv.Itoa(partition) + "}"
				resp := callSingleFC(client, serviceName, functionName, newInput, qualifier)
				lock.Lock()
				response = append(response, resp)
				lock.Unlock()
				defer wg.Done()
			}(inputParam, i)
		}
		wg.Wait()

		//consumeTime := time.Since(startTime)
		//fmt.Println("lock run time:", consumeTime)
		//fmt.Println(response)

		/*
			//channel实现方案
			startTime := time.Now()
			responseChan := make(chan string)
			for i := 0; i < partitions; i++ {
				go func(eachInput string, partition int, innerChan chan string) {
					// 添加分片号
					newInput := eachInput[:len(eachInput)-1] + `,"partition":` + strconv.Itoa(partition) + "}"
					innerChan <- callSingleFC(client, serviceName, functionName, newInput, qualifier)
				}(inputParam, i, responseChan)
			}

			// 接受响应
			for j := 0; j < partitions; j++ {
				resp := <-responseChan
				response = append(response, resp)
			}
			consumeTime := time.Since(startTime)
			fmt.Println("channel run time:", consumeTime)
			fmt.Println(response)
		*/

		// 清空
		//response = response[:0]

	}

	return response
}
