import os.path

from service.service_action import BaseServiceAction


class JavaServiceAction(BaseServiceAction):

    def run(self):
        download_path = self.download(self.config.service + ".jar")

        target = os.path.join(
            self.wefe_dir,
            self.config.service + ".jar"
        )
        self.replace_file(download_path, target)
        pass
