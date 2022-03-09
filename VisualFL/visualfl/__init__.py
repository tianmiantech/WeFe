
import os

__version__ = "1.0"
__basedir__ = os.path.dirname(os.path.abspath(__file__))
__logs_dir__ = os.path.abspath(os.path.join(__basedir__, os.path.pardir, "logs"))
__config_path__ = os.path.abspath(os.path.join(__basedir__, os.path.pardir, "config.properties"))
# __fl_job_config_dir__ = os.path.abspath(os.path.join(__basedir__, os.path.pardir, "visualfl/fl_job_config"))
__data_dir__ = os.path.abspath(os.path.join(__basedir__, os.path.pardir, "data"))

VISUALFL_DATA_BASE_ENV = "VISUALFL_DATA_BASE_ENV"


def get_data_dir():
    if VISUALFL_DATA_BASE_ENV in os.environ and os.path.exists(
        os.environ.get(VISUALFL_DATA_BASE_ENV)
    ):
        return os.path.abspath(os.environ.get(VISUALFL_DATA_BASE_ENV))
    else:
        return __data_dir__


if __name__ == '__main__':
    print(get_data_dir())