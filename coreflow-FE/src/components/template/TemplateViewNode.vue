<template>
  <div :class="['custom-node', { highlighted: data.highlight }]">
    <Handle id="target" type="target" position="left" class="handle" />

    <div class="content">
      <div class="label">{{ data.label }}</div>
      <div class="description">{{ data.description }}</div>
      <div class="durtaion">🕒 소요: {{ data.duration }}일</div>
      <div v-if="data.slackTime" class="slack">⏱ 여유: {{ data.slackTime }}일</div>
      
      <div class="dept">📂
        {{
          data.deptList?.slice(0, 2).map(d =>
            typeof d === 'object' ? (d.name || d.deptName || '') : d
          ).join(', ')
        }}
        <span v-if="data.deptList?.length > 2">+{{ data.deptList.length - 2 }}</span>
      </div>
    </div>

    <Handle id="source" type="source" position="right" class="handle" />
  </div>
</template>


<script setup>
import { Handle } from '@vue-flow/core'


const props = defineProps({
  data: Object
})

</script>

<style scoped>
.custom-node {
  background: #f9f9ff;
  border: 1px solid #aaa;
  border-radius: 8px;
  box-shadow: 1px 1px 4px rgba(0, 0, 0, 0.1);
  width: 240px;
  min-height: 130px;
  box-sizing: border-box;
  padding: 12px;
  position: relative;
  text-align: left;
}
/* 부서별 태스크 하이라이트 */
.custom-node.highlighted {
  border: 2px solid #2196f3;
  background-color: #e3f2fd;
  box-shadow: 0 0 8px rgba(33, 150, 243, 0.4);
}
.content {
  font-size: 13px;
}
.label {
  font-weight: bold;
  margin-bottom: 4px;
}
.description {
  font-size: 12px;
  color: #444;
  margin-bottom: 6px;
}
.dept {
  font-size: 11px;
  color: #777;
}
.slack .duration {
  font-size: 11px;
  color: #555;
  margin-top: 4px;
}
.handle {
  width: 10px;
  height: 10px;
  background: #555;
  border-radius: 50%;
}
</style>
