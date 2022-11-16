<template>
  <el-tag
    v-if="status === 'success'"
    size="mini"
    class="task_status_tag"
    type="success"
    effect="dark"
  >
    成功
  </el-tag>
  <el-tag
    v-else-if="status === 'running'"
    size="mini"
    class="task_status_tag"
    effect="light"
  >
    运行中
  </el-tag>
  <el-tag
    v-else
    size="mini"
    type="danger"
    class="task_status_tag"
    effect="dark"
  >
    失败
  </el-tag>
</template>

<script>
export default {
  props: {
    status: String,
  },
};
</script>
<style lang="scss">
.el-tag.task_status_tag {
  display: inline-block;
  width: 50px;
  text-align: center;
}
</style>
