


class VisualFLBaseException(BaseException):
    ...


class VisualFLException(VisualFLBaseException, Exception):
    ...


class VisualFLExtensionException(VisualFLException):
    ...


class VisualFLWorkerException(VisualFLException):
    ...


class VisualFLJobCompileException(VisualFLException):
    ...


class VisualFLDataNotFoundException(VisualFLException):
    ...
