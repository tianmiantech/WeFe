

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