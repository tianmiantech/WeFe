<template>
  <el-card v-loading="loading" shadow="never" class="page">
    <el-form :model="form" label-position="top" style="width: 400px">
      <el-form-item prop="modelId" label="任务ID：">
        {{ task.task_id }}
      </el-form-item>

      <el-form-item prop="modelId" label="模型ID：">
        {{ task.model_id }}
      </el-form-item>

      <el-form-item label="样本量：" label-width="100">
        {{ task.total }}
      </el-form-item>

      <el-form-item
        v-if="task.status === 'fail'"
        label="任务进度："
        label-width="100"
      >
        <el-progress :percentage="50" status="exception" />
      </el-form-item>

      <el-form-item
        v-if="task.status === 'running'"
        label="任务进度："
        label-width="100"
      >
        <el-progress :percentage="progress || 0"/>
      </el-form-item>

      <el-form-item
        v-if="task.status === 'success'"
        label="任务进度："
        label-width="100"
      >
        <el-progress :percentage="100" status="success" />
      </el-form-item>

      <el-form-item
        v-if="task.status === 'success' && task.fail_count !== task.total"
        prop="模型分已输出到"
        label="模型分已输出到："
      >
        {{ task.dist_file }}
        <el-button :disabled="!task.dist_file" @click="download">
          下载
        </el-button>
      </el-form-item>
      <el-form-item
        v-if="task.fail_count !== 0"
        prop="部分样本预测失败"
        label="部分样本预测失败："
      >
        {{ task.error_file }}
        <el-button :disabled="!task.error_file" @click="downloadError">
          导出
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script>
import { mapGetters } from "vuex";

export default {
  data() {
    return {
      progress: 0,
      loading: false,
      form: {
        modelId: "",
        filename: "",
        distFilename: "",
      },

      task: {
        id: "",
        task_id: "",
        model_id: "",
        error_file: "",
        dist_file: "",
        success_count: "",
        fail_count: "",
        total: "",
        status: "",
        progress: 0,
      },

      timer: null,
    };
  },
  computed: {
    ...mapGetters(["userInfo"]),
  },
  created() {
    this.getData();
  },

  beforeDestroy() {
    clearInterval(this.timer);
    this.timer = null;
  },
  methods: {
    async getData() {
      this.loading = true;
      const { code, data } = await this.$http.get({
        url: "predict/task/detail",
        params: {
          taskId: this.$route.query.id,
        },
      });

      this.loading = false;
      if (code === 0) {
        this.task = data;

        if (this.task.status === "running") {
          this.timer = setTimeout(this.getTaskInfo);
        }
      }
    },

    async getTaskInfo() {
      this.running = true;
      const { code, data } = await this.$http.get({
        url: "predict/task_info",
        params: {
          task_id: this.task.task_id,
        },
      });

      setTimeout((_) => {
        this.running = false;
      }, 1000);
      if (code === 0) {
        console.log(data);
        this.task.fail_count = data.fail_count;
        this.task.success_count = data.success_count;
        this.progress = data.progress;
        this.task.status = data.status;

        if (data.status !== "running") {
          this.getData();
          clearTimeout(this.timer);
        } else {
          this.timer = setTimeout(this.getTaskInfo, 3000);
        }
      }
    },

    download(e) {
      const href = `${window.api.baseUrl}/predict/file_export?path=${this.task.dist_file}&token=${this.userInfo.token}`;
      const link = document.createElement("a");

      link.href = href;
      link.target = "_blank";
      link.style.display = "none";
      document.body.appendChild(link);
      link.click();
    },
    downloadError(e) {
      const href = `${window.api.baseUrl}/predict/file_export?path=${this.task.error_file}&token=${this.userInfo.token}`;
      const link = document.createElement("a");

      link.href = href;
      link.target = "_blank";
      link.style.display = "none";
      document.body.appendChild(link);
      link.click();
    },
  },
};
</script>
