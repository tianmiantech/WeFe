# Copyright 2021 Tianmian Tech. All Rights Reserved.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.



from __future__ import print_function

from visualfl.utils import data_loader
import unittest


class TestDataLoader(unittest.TestCase):
    def check_reader(self, reader):
        sum = 0
        label = 0
        size = 224 * 224 * 3
        for l in reader():
            self.assertEqual(l[0].size, size)
            if l[1] > label:
                label = l[1]
            sum += 1
        return sum, label

    def test_train(self):
        instances, max_label_value = self.check_reader(
            data_loader.train())
        self.assertEqual(instances, 7169)
        self.assertEqual(max_label_value, 101)

    def test_test(self):
        instances, max_label_value = self.check_reader(
            data_loader.test())
        self.assertEqual(instances, 1020)
        self.assertEqual(max_label_value, 101)

    def test_valid(self):
        instances, max_label_value = self.check_reader(
            data_loader.valid())
        self.assertEqual(instances, 1020)
        self.assertEqual(max_label_value, 101)


if __name__ == '__main__':
    unittest.main()