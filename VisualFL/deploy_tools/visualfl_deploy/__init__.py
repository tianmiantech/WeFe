

from pathlib import Path

__base_dir__ = Path(__file__).parent
__visualfl_tarball__ = __base_dir__.joinpath("data", "visualfl.tar.gz").absolute()
__template__ = __base_dir__.joinpath("template").absolute()
__BASE_NAME__ = "VisualFL"
