<template>
  <el-card class="page" shadow="never">
    <el-form class="mb20" inline>
      <el-form-item label="任务id:">
        <el-input v-model="search.task_id" clearable />
      </el-form-item>

      <el-form-item label="模型id:">
        <el-input v-model="search.model_id" clearable />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="getList({ to: true })">
          查询
        </el-button>
        <router-link class="ml10" :to="{ name: 'serving-batch-add' }">
          <el-button size="small" type="primary" plain> 创建任务 </el-button>
        </router-link>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" stripe border>

      <el-table-column label="任务" width="240">
        <template slot-scope="scope">
          <p class="id">{{ scope.row.task_id }}</p>
        </template>
      </el-table-column>

      <el-table-column label="模型ID" width="438">
        <template slot-scope="scope">
          <p class="id">{{ scope.row.model_id }}</p>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="80">
        <template slot-scope="scope">
          <TaskStatusTag :status="scope.row.status" />
        </template>
      </el-table-column>

      <el-table-column label="数据量" prop="total" width="80" />

      <el-table-column label="成功数量" prop="success_count" width="80" />

      <el-table-column label="失败数量" prop="fail_count" width="80" />

      <el-table-column label="创建时间" min-width="120">
        <template slot-scope="scope">
          {{ scope.row.created_time | dateFormat }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="100">
        <template slot-scope="scope">
          <router-link
            :to="{
              name: 'serving-batch-view',
              query: { id: scope.row.task_id },
            }"
          >
            <el-button size="small" type="primary"> 详情 </el-button>
          </router-link>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="pagination.total" class="mt20 text-r">
      <el-pagination
        :total="pagination.total"
        :page-sizes="[10, 20, 30, 40, 50]"
        :page-size="pagination.page_size"
        :current-page="pagination.page_index"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="currentPageChange"
        @size-change="pageSizeChange"
      />
    </div>
  </el-card>
</template>

<script>
import table from "@src/mixins/table.js";
import TaskStatusTag from "../components/task-status-tag";

export default {
  components: {
    TaskStatusTag,
  },
  mixins: [table],
  data() {
    return {
      search: {
        task_id: "",
        model_id: "",
      },

      headers: {
        token: localStorage.getItem("token") || "",
      },
      getListApi: "predict/task/list",
    };
  },
  async created() {
    if (this.$route.query.type === "my_role") {
      this.search.my_role = this.$route.query.value;
    } else {
      this.search.status = this.$route.query.value;
    }

    this.getList();
  },
  methods: {
    async deleteTask(id) {
      this.$confirm("此操作将永久删除该条目, 是否继续?", "警告", {
        type: "warning",
      }).then(async () => {
        const { code } = await this.$http.post({
          url: "/task/delete",
          data: {
            id,
          },
        });

        if (code === 0) {
          this.$message("删除成功!");
          this.getList();
        }
      });
    },
  },
};
</script>
