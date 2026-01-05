<script setup lang="ts">
import { ref } from 'vue'
import { AppCard } from '@/components/common'

const activeNames = ref<string[]>([])

const faqs = [
  {
    id: '1',
    question: '如何预约寄养服务？',
    answer: '您可以在机构详情页选择合适的服务套餐，填写预约信息并完成支付即可。预约成功后，您会收到确认通知。'
  },
  {
    id: '2',
    question: '如何取消预约？',
    answer: '在"我的订单"中找到对应订单，点击"取消订单"即可。请注意：入住前48小时以上取消可全额退款，48小时内取消退款50%。'
  },
  {
    id: '3',
    question: '寄养期间如何了解宠物状态？',
    answer: '机构会每天更新宠物的健康状态和活动照片，您可以在订单详情中查看。如有异常情况，机构会第一时间通知您。'
  },
  {
    id: '4',
    question: '如何选择合适的寄养机构？',
    answer: '建议您根据机构评分、用户评价、服务内容和价格综合考虑。您也可以实地考察机构环境后再做决定。'
  },
  {
    id: '5',
    question: '宠物需要准备什么？',
    answer: '建议准备：疫苗本、宠物日常用品（如专用食盆、玩具）、常用药品（如有）。具体要求可咨询预约的机构。'
  },
  {
    id: '6',
    question: '如何联系客服？',
    answer: '您可以通过APP内的在线客服功能联系我们，或拨打客服热线：400-XXX-XXXX（工作时间：9:00-21:00）。'
  }
]

const categories = [
  { icon: '📋', title: '预约相关', desc: '预约流程、修改、取消' },
  { icon: '💰', title: '支付退款', desc: '支付方式、退款政策' },
  { icon: '🐾', title: '寄养服务', desc: '服务内容、注意事项' },
  { icon: '⭐', title: '评价系统', desc: '如何评价、查看评价' }
]
</script>

<template>
  <div class="help-center-view">
    <div class="page-header">
      <h1>帮助中心</h1>
      <p>有问题？我们来帮您解答</p>
    </div>
    
    <!-- 分类入口 -->
    <div class="category-grid">
      <AppCard 
        v-for="cat in categories" 
        :key="cat.title"
        class="category-card"
        hoverable
        shadow="sm"
      >
        <div class="category-icon">{{ cat.icon }}</div>
        <h3>{{ cat.title }}</h3>
        <p>{{ cat.desc }}</p>
      </AppCard>
    </div>
    
    <!-- 常见问题 -->
    <section class="faq-section">
      <h2>常见问题</h2>
      <el-collapse v-model="activeNames" accordion>
        <el-collapse-item 
          v-for="faq in faqs" 
          :key="faq.id"
          :title="faq.question"
          :name="faq.id"
        >
          <p>{{ faq.answer }}</p>
        </el-collapse-item>
      </el-collapse>
    </section>
    
    <!-- 联系我们 -->
    <AppCard class="contact-card" shadow="md" padding="lg">
      <h3>没有找到答案？</h3>
      <p>联系我们的客服团队，我们将尽快为您解答</p>
      <div class="contact-info">
        <div class="contact-item">
          <span class="icon">📞</span>
          <span>客服热线：400-XXX-XXXX</span>
        </div>
        <div class="contact-item">
          <span class="icon">📧</span>
          <span>邮箱：support@petfoster.com</span>
        </div>
        <div class="contact-item">
          <span class="icon">⏰</span>
          <span>工作时间：9:00 - 21:00</span>
        </div>
      </div>
    </AppCard>
  </div>
</template>

<style lang="scss" scoped>
.help-center-view {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  
  h1 {
    font-family: var(--font-display);
    font-size: 32px;
    margin: 0 0 8px;
  }
  
  p {
    color: var(--color-text-secondary);
    margin: 0;
  }
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 48px;
  
  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.category-card {
  text-align: center;
  
  .category-icon {
    font-size: 32px;
    margin-bottom: 12px;
  }
  
  h3 {
    font-size: 16px;
    margin: 0 0 4px;
  }
  
  p {
    font-size: 13px;
    color: var(--color-text-muted);
    margin: 0;
  }
}

.faq-section {
  margin-bottom: 48px;
  
  h2 {
    font-family: var(--font-display);
    font-size: 24px;
    margin: 0 0 24px;
  }
  
  :deep(.el-collapse-item__header) {
    font-weight: 500;
    font-size: 15px;
  }
  
  :deep(.el-collapse-item__content) {
    color: var(--color-text-secondary);
    line-height: 1.6;
  }
}

.contact-card {
  text-align: center;
  
  h3 {
    font-size: 20px;
    margin: 0 0 8px;
  }
  
  p {
    color: var(--color-text-secondary);
    margin: 0 0 24px;
  }
}

.contact-info {
  display: flex;
  justify-content: center;
  gap: 40px;
  flex-wrap: wrap;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .icon {
    font-size: 20px;
  }
}
</style>
