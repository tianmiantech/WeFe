# python代码规范整理
> 简单总结：采用PEP8的代码检测规范(IDEA,Pycharm自带)，统一注释、统一命名

> 详细请查看： https://www.runoob.com/w3cnote/google-python-styleguide.html

## 1. IDE
> 尽量采用IDEA或者Pycharm开发，自带了PEP8的代码检测规范。其他工具需要自行安装PEP8的检测插件

> 设置Docstring format， Preferences -> Tools -> Python Integrated Tools -> 【Wefe】模块 -> Docstring format选择NumPy

> 默认PEP8的行长度是80个字符，IDEA默认是120，我们也统一采用120

## 2. 注释
### 2.1 文档字符串
>Python有一种独一无二的的注释方式: 使用文档字符串. 文档字符串是包, 模块, 类或函数里的第一个语句. 这些字符串可以通过对象的__doc__成员被自动提取, 并且被pydoc所用. (你可以在你的模块上运行pydoc试一把, 看看它长什么样). 我们对文档字符串的惯例是使用三重双引号"""

### 2.2 模块
>每个文件应该包含一个许可样板. 根据项目使用的许可(例如, Apache 2.0, BSD, LGPL, GPL), 选择合适的样板.

### 2.3 函数和方法
>文档字符串应该包含函数做什么, 以及输入和输出的详细描述. 

函数参考注释
```python
def fetch_bigtable_rows(big_table, keys, other_silly_variable=None):
    """ Fetches rows from a Bigtable.

    Retrieves rows pertaining to the given keys from the Table instance
    represented by big_table.  Silly things may happen if
    other_silly_variable is not None.

    Parameters
    ----------
    keys: string
        xxx 
    other_silly_variable: string
        xxx
        
    Returns
    -------
    A dict mapping keys to the corresponding table row data
    fetched. Each row is represented as a tuple of strings. For
    example:

    {'Serak': ('Rigel VII', 'Preparer'),
     'Zim': ('Irk', 'Invader'),
     'Lrrr': ('Omicron Persei 8', 'Emperor')}

    If a key from the keys argument is missing from the dictionary,
    then that row was not found in the table.

    Raises:
    ----------
    IOError: An error occurred accessing the bigtable.Table object.
    
    """
    pass
```

### 2.4 类注释
>类应该在其定义下有一个用于描述该类的文档字符串. 如果你的类有公共属性(Attributes), 那么文档中应该有一个属性(Attributes)段. 并且应该遵守和函数参数相同的格式

类参考注释
```python
class SampleClass(object):
    """Summary of class here.

    Longer class information....
    Longer class information....

    Attributes:
        likes_spam: A boolean indicating if we like SPAM or not.
        eggs: An integer count of the eggs we have laid.
    """

    def __init__(self, likes_spam=False):
        """Inits SampleClass with blah."""
        self.likes_spam = likes_spam
        self.eggs = 0

    def public_method(self):
        """Performs operation blah."""

```

### 2.5 块注释和行注释
>最需要写注释的是代码中那些技巧性的部分. 如果你在下次 代码审查 的时候必须解释一下, 那么你应该现在就给它写注释. 对于复杂的操作, 应该在其操作开始前写上若干行注释. 对于不是一目了然的代码, 应在其行尾添加注释

块注释参考
```python
# We use a weighted dictionary search to find out where i is in
# the array.  We extrapolate position based on the largest num
# in the array and the array size and then do binary search to
# get the exact number.

if i & (i-1) == 0:        # true iff i is a power of 2
    pass
```

## 3. 导入格式
> 每个导入应该独占一行
> 导入总应该放在文件顶部, 位于模块注释和文档字符串之后, 模块全局变量和常量之前. 导入应该按照从最通用到最不通用的顺序分组:
- 标准库导入
- 第三方库导入
- 应用程序指定导入

导入参考：
```python
import foo
from foo import bar
from foo.bar import baz
from foo.bar import Quux
from Foob import ar
```

## 4. 命名规范
>module_name, package_name, ClassName, method_name, ExceptionName, function_name, GLOBAL_VAR_NAME, instance_var_name, function_parameter_name, local_var_name.

### 4.1 命名约定
- "内部(Internal)"表示仅模块内可用, 或者, 在类内是保护或私有的.
- 用单下划线(_)开头表示模块变量或函数是protected的(使用import * from时不会包含).
- 用双下划线(__)开头的实例变量或方法表示类内私有.
- 对类名使用大写字母开头的单词(如CapWords, 即Pascal风格), 但是模块名应该用小写加下划线的方式(如lower_with_under.py).


### 4.2 推荐的规范

| Type                       | Public             | Internal                                                          |
|----------------------------|--------------------|-------------------------------------------------------------------|
| Modules                    | lower_with_under   | _lower_with_under                                                 |
| Packages                   | lower_with_under   |                                                                   |
| Classes                    | CapWords           | _CapWords                                                         |
| Exceptions                 | CapWords           |                                                                   |
| Functions                  | lower_with_under() | _lower_with_under()                                               |
| Global/Class Constants     | CAPS_WITH_UNDER    | _CAPS_WITH_UNDER                                                  |
| Global/Class Variables     | lower_with_under   | _lower_with_under                                                 |
| Instance Variables         | lower_with_under   | _lower_with_under (protected) or __lower_with_under (private)     |
| Method Names               | lower_with_under() | _lower_with_under() (protected) or __lower_with_under() (private) |
| Function/Method Parameters | lower_with_under   |                                                                   |
| Local Variables            | lower_with_under   |                                                                   |


## 5. python声明
> 大部分.py文件不必以#!作为文件的开始. 根据 PEP-394 , 程序的main文件应该以 #!/usr/bin/python2或者 #!/usr/bin/python3开始.

## 6. 缩进
> 用4个空格来缩进代码。在IDEA已经默认已经把tab替换成4个空格。

## 7. 空行
> 顶级定义之间空两行, 方法定义之间空一行。 使用IDEA的自动格式化即可。

## 8. 空格
> 按照标准的排版规范来使用标点两边的空格.括号内不要有空格.使用IDEA的自动格式化即可。

