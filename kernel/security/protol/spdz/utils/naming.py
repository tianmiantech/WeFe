import _md5


class NamingService(object):
    __instance = None

    @classmethod
    def get_instance(cls):
        if cls.__instance is None:
            raise EnvironmentError("naming service not set")
        return cls.__instance

    @classmethod
    def set_instance(cls, instance):
        prev = cls.__instance
        cls.__instance = instance
        return prev

    def __init__(self, init_name="ss"):
        self._name = _md5.md5(init_name.encode("utf-8")).hexdigest()

    def next(self):
        self._name = _md5.md5(self._name.encode("utf-8")).hexdigest()
        return self._name
