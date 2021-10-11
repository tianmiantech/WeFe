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

import com.welab.wefe.common.data.storage.StorageManager;
import com.welab.wefe.common.data.storage.service.StorageService;

/**
 * @author yuxin.zhang
 */
public class StorageTest {
    static String dbName = "wefe_yuxin";
    static String tbName = "yuxin_test";
    public static StorageService repo;

    static {
        StorageManager.getInstance().init();
        repo = StorageManager.getInstance().getRepo(StorageService.class);
    }

    public static void main(String[] args) {
        repo.dropTB(dbName, tbName);
        int count = repo.count(dbName, tbName);
        System.out.println(count);
        saveAllByCSV();
        count = repo.count(dbName, tbName);
        System.out.println(count);
//        DataItemModel storageModel = repo.getByKey(dbName, tbName, key);
//        System.out.println(JSON.toJSONString(storageModel));
//        List<DataItemModel> data = repo.getList(dbName, tbName);
//        System.out.println(JSON.toJSONString(data));

//        PageOutputModel pageOutputModel = repo.getPage(dbName, tbName, new PageInputModel(0, 10));
//        System.out.println(JSON.toJSONString(pageOutputModel));
//        PageOutputModel pageOutputModel1 = repo.getPage(dbName, tbName, new PageInputModel(1, 10));
//        System.out.println(JSON.toJSONString(pageOutputModel1));
    }


    public static void saveAllByCSV() {
//        CsvReader csvReader;
//        try {
//            long start = System.currentTimeMillis();
//
//            List<DataItemModel<String, String>> data = null;
//            csvReader = new CsvReader(new File("G://big.csv").getAbsolutePath(), "UTF-8");
//            Iterator<String[]> iterator = csvReader.iterator();
//            while (iterator.hasNext()) {
//                if (data == null) {
//                    data = new ArrayList<>();
//                    iterator.next();
//                    continue;
//                }
//                if (data.size() == 10000) {
//                    repo.saveList(dbName, tbName, data);
//                    System.out.println(System.currentTimeMillis());
//                    data = new ArrayList<>();
//                }
//                String[] row = iterator.next();
//                String y = StringUtil.join(Arrays.stream(row).collect(Collectors.toList()));
//                data.add(new DataItemModel<>(row[0], y));
//            }
//            repo.saveList(dbName, tbName, data);
//
//            System.out.println("spend time:" + (System.currentTimeMillis() - start));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

    }
}
