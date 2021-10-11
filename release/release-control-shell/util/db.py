from peewee import *

database = MySQLDatabase('wefe_board', **{'charset': 'utf8', 'sql_mode': 'PIPES_AS_CONCAT', 'use_unicode': True, 'host': '10.1.0.120', 'port': 3306, 'user': 'wefe', 'password': 'O*****DDx'})

class UnknownField(object):
    def __init__(self, *_, **__): pass

class BaseModel(Model):
    class Meta:
        database = database

