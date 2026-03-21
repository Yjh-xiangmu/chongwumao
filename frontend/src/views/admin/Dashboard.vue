<template>
  <div class="dashboard-container" v-loading="loading">

    <!-- 顶部统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <div class="stat-card" :style="{ background: card.bg }">
          <div class="stat-icon">{{ card.icon }}</div>
          <div class="stat-right">
            <div class="stat-num">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 第二行小卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="card in statCards2" :key="card.label">
        <div class="stat-card stat-card-sm" :style="{ background: card.bg }">
          <div class="stat-icon sm">{{ card.icon }}</div>
          <div class="stat-right">
            <div class="stat-num sm">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 折线图：近7天申请趋势 -->
      <el-col :span="15">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <span class="chart-title">📈 近 7 天领养申请趋势</span>
          </template>
          <canvas ref="lineChartRef" height="100"></canvas>
        </el-card>
      </el-col>

      <!-- 饼图：申请状态分布 -->
      <el-col :span="9">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <span class="chart-title">🥧 申请状态分布</span>
          </template>
          <canvas ref="pieChartRef" height="180"></canvas>
        </el-card>
      </el-col>
    </el-row>

  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const loading = ref(false)
const lineChartRef = ref(null)
const pieChartRef = ref(null)

const statCards  = ref([])
const statCards2 = ref([])

// 动态加载 Chart.js
const loadChartJs = () => new Promise((resolve) => {
  if (window.Chart) { resolve(window.Chart); return }
  const script = document.createElement('script')
  script.src = 'https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.min.js'
  script.onload = () => resolve(window.Chart)
  document.head.appendChild(script)
})

onMounted(async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/stats/dashboard')
    if (res.data.code !== 200) return
    const d = res.data.data

    statCards.value = [
      { icon: '🐱', label: '猫咪总数',   value: d.totalCats,     bg: 'linear-gradient(135deg,#fff9e6,#ffeedb)' },
      { icon: '✅', label: '待领养',      value: d.availableCats, bg: 'linear-gradient(135deg,#f0fff4,#d4edda)' },
      { icon: '🏠', label: '已领养',      value: d.adoptedCats,   bg: 'linear-gradient(135deg,#e8f4fd,#cce5ff)' },
      { icon: '📋', label: '申请总数',    value: d.totalApps,     bg: 'linear-gradient(135deg,#fff3e0,#ffe0b2)' },
    ]
    statCards2.value = [
      { icon: '👤', label: '领养人数',    value: d.totalAdopters,   bg: 'linear-gradient(135deg,#f3e5f5,#e1bee7)' },
      { icon: '🙋', label: '救助员数',    value: d.totalVolunteers, bg: 'linear-gradient(135deg,#e0f7fa,#b2ebf2)' },
      { icon: '⏳', label: '待审核申请',  value: d.pendingApps,     bg: 'linear-gradient(135deg,#fce4ec,#f8bbd0)' },
      { icon: '💬', label: '社区帖子数',  value: d.totalPosts,      bg: 'linear-gradient(135deg,#e8eaf6,#c5cae9)' },
    ]

    await nextTick()
    const Chart = await loadChartJs()

    // 折线图
    new Chart(lineChartRef.value, {
      type: 'line',
      data: {
        labels: d.chartDays,
        datasets: [{
          label: '新增申请',
          data: d.chartCounts,
          borderColor: '#ff9d4d',
          backgroundColor: 'rgba(255,157,77,0.12)',
          borderWidth: 2.5,
          pointBackgroundColor: '#ff9d4d',
          pointRadius: 4,
          tension: 0.4,
          fill: true,
        }]
      },
      options: {
        responsive: true,
        plugins: { legend: { display: false } },
        scales: {
          y: {
            beginAtZero: true,
            ticks: { stepSize: 1, precision: 0 },
            grid: { color: 'rgba(0,0,0,0.05)' }
          },
          x: { grid: { display: false } }
        }
      }
    })

    // 饼图
    new Chart(pieChartRef.value, {
      type: 'doughnut',
      data: {
        labels: d.pieData.map(i => i.name),
        datasets: [{
          data: d.pieData.map(i => i.value),
          backgroundColor: ['#909399','#E6A23C','#F56C6C','#67C23A','#C0C4CC'],
          borderWidth: 2,
          borderColor: '#fff',
        }]
      },
      options: {
        responsive: true,
        cutout: '60%',
        plugins: {
          legend: {
            position: 'bottom',
            labels: { padding: 16, font: { size: 12 } }
          }
        }
      }
    })

  } catch (e) {
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dashboard-container { padding-bottom: 20px; }

.stat-row { margin-bottom: 16px; }

.stat-card {
  border-radius: 16px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 18px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  transition: transform 0.2s;
}
.stat-card:hover { transform: translateY(-3px); }
.stat-card-sm { padding: 16px 20px; }

.stat-icon { font-size: 36px; line-height: 1; }
.stat-icon.sm { font-size: 28px; }
.stat-right { flex: 1; }
.stat-num { font-size: 32px; font-weight: bold; color: #333; line-height: 1.1; }
.stat-num.sm { font-size: 26px; }
.stat-label { font-size: 13px; color: #888; margin-top: 4px; }

.chart-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.chart-title { font-size: 15px; font-weight: bold; color: #444; }
</style>