<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores'
import { useAIStore } from '@/stores/ai'
import { institutionApi } from '@/api/institution'
import type { InstitutionWithDistance } from '@/types/institution'
import InstitutionCard from '@/components/institution/InstitutionCard.vue'
import InstitutionCardSkeleton from '@/components/institution/InstitutionCardSkeleton.vue'
import { AppButton } from '@/components/common'
import AIFloatingButton from '@/components/ai/AIFloatingButton.vue'
import AIChatWindow from '@/components/ai/AIChatWindow.vue'

const router = useRouter()
const authStore = useAuthStore()
const isAuthenticated = computed(() => authStore.isAuthenticated)
const searchKeyword = ref('')
const loading = ref(false)
const featuredInstitutions = ref<InstitutionWithDistance[]>([])
const scrollY = ref(0)

// 滚动动画相关
let observer: IntersectionObserver | null = null

const setupScrollAnimations = () => {
  observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('animate-in')
      } else {
        // 离开视口时移除动画类，下次进入时重新触发
        entry.target.classList.remove('animate-in')
      }
    })
  }, {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
  })
  
  // 观察所有需要动画的元素
  document.querySelectorAll('.scroll-animate').forEach(el => {
    observer?.observe(el)
  })
}

const handleScroll = () => { scrollY.value = window.scrollY }
onMounted(() => { 
  window.addEventListener('scroll', handleScroll)
  fetchFeatured()
  // 延迟设置动画观察器，确保DOM已渲染
  setTimeout(setupScrollAnimations, 100)
})
onUnmounted(() => { 
  window.removeEventListener('scroll', handleScroll)
  observer?.disconnect()
})

const processSteps = [
  { icon: '🔍', title: '发现', desc: '智能搜索附近优质机构', color: '#FF9F7F' },
  { icon: '💬', title: '了解', desc: '查看环境、评价与价格', color: '#7FD4C1' },
  { icon: '📅', title: '预约', desc: '在线选择日期并下单', color: '#A8D4FF' },
  { icon: '🏡', title: '托付', desc: '安心送宠，实时追踪', color: '#FFD4A8' }
]

const testimonials = ref([
  { id: 1, avatar: '👩', name: '李女士', pet: '金毛 · 旺财', rating: 5, content: '第一次寄养很担心，但机构每天发视频，旺财玩得很开心！', bgColor: '#FFF5F0' },
  { id: 2, avatar: '👨', name: '张先生', pet: '英短 · 小灰', rating: 5, content: '猫咪比较胆小，工作人员很有耐心，还给准备了独立的安静空间。', bgColor: '#F0FFF5' },
  { id: 3, avatar: '👩‍🦰', name: '王小姐', pet: '柯基 · 肉肉', rating: 5, content: '价格透明，环境干净，还有专门的活动区域。肉肉回来后精神状态特别好！', bgColor: '#F5F0FF' }
])

const serviceTypes = [
  { icon: '🐕', name: '狗狗寄养', desc: '专业犬舍，每日遛弯', emoji: '🦴' },
  { icon: '🐱', name: '猫咪寄养', desc: '独立猫房，安静舒适', emoji: '🐟' },
  { icon: '🐰', name: '小宠寄养', desc: '兔子、仓鼠等小动物', emoji: '🥕' },
  { icon: '🏥', name: '医疗护理', desc: '特殊护理，专业照顾', emoji: '💊' }
]

const quickLinks = [
  { icon: '🗺️', name: '地图找店', desc: '查看附近机构', route: '/map' },
  { icon: '🤖', name: 'AI 助手', desc: '智能问答服务', route: '', action: 'openAI' },
  { icon: '❓', name: '帮助中心', desc: 'FAQ与使用指南', route: '/help' },
  { icon: '📝', name: '提交工单', desc: '问题反馈与咨询', route: '/support/ticket' }
]

const faqs = ref([
  { q: '寄养需要准备什么？', a: '需要携带宠物疫苗本、常用食物、喜欢的玩具。机构会提供基础用品。', open: false },
  { q: '如何确保宠物安全？', a: '所有机构经过平台认证，提供24小时监控，并购买宠物意外险。', open: false },
  { q: '可以随时查看宠物状态吗？', a: '是的，机构每天会发送宠物照片/视频，部分机构支持实时监控。', open: false },
  { q: '取消预约如何退款？', a: '入住前24小时取消可全额退款，24小时内取消收取30%手续费。', open: false }
])

const toggleFaq = (index: number) => { faqs.value[index].open = !faqs.value[index].open }
const handleSearch = () => { router.push({ name: 'institutionList', query: { keyword: searchKeyword.value } }) }
const goToQuickLink = (link: typeof quickLinks[0]) => {
  if (link.action === 'openAI') { useAIStore().setOpen(true) } 
  else if (link.route) { router.push(link.route) }
}

const fetchFeatured = async () => {
  loading.value = true
  try {
    const res = await institutionApi.search({ sortBy: 'rating', pageSize: 4 })
    if (res.code === 200 && res.data) { featuredInstitutions.value = res.data.list }
  } catch (e) { console.error('Failed to fetch featured:', e) }
  finally { loading.value = false }
}
</script>

<template>
  <div class="home-view">
    <section class="hero">
      <div class="hero__blobs">
        <div class="blob blob--1" :style="{ transform: `translateY(${scrollY * 0.1}px)` }"></div>
        <div class="blob blob--2" :style="{ transform: `translateY(${scrollY * -0.05}px)` }"></div>
        <div class="blob blob--3" :style="{ transform: `translateY(${scrollY * 0.08}px)` }"></div>
      </div>
      <div class="hero__noise"></div>
      <div class="hero__content">
        <div class="hero__left">
          <div class="hero__tag"><span class="hero__tag-dot"></span>宠物寄养新体验</div>
          <h1 class="hero__title">让每一次分别<span class="hero__title-highlight"><span class="hero__title-text">都是安心的托付</span><svg class="hero__title-underline" viewBox="0 0 200 12" preserveAspectRatio="none"><path d="M0,8 Q50,0 100,8 T200,8" fill="none" stroke="currentColor" stroke-width="3"/></svg></span></h1>
          <p class="hero__desc">500+ 认证机构 · 10万+ 宠物主人信赖<br/>专业、透明、有温度的寄养服务平台</p>
          <div class="hero__search">
            <div class="hero__search-inner">
              <span class="hero__search-icon">🔍</span>
              <input v-model="searchKeyword" type="text" placeholder="搜索地区或机构名称..." @keyup.enter="handleSearch"/>
              <button class="hero__search-btn" @click="handleSearch">搜索</button>
            </div>
            <div class="hero__search-tags"><span>热门:</span><router-link to="/institutions?city=北京">北京</router-link><router-link to="/institutions?city=上海">上海</router-link><router-link to="/institutions?city=广州">广州</router-link></div>
          </div>
        </div>
        <div class="hero__right">
          <div class="hero__cards">
            <div class="hero__card hero__card--main"><div class="hero__card-avatar">🏠</div><div class="hero__card-content"><span class="hero__card-label">温馨小窝</span><span class="hero__card-status"><span class="status-dot status-dot--active"></span>3只宠物入住中</span></div></div>
            <div class="hero__card hero__card--float hero__card--1"><span class="hero__card-emoji">🐕</span><div><span class="hero__card-name">旺财</span><span class="hero__card-state">正在玩耍 🎾</span></div></div>
            <div class="hero__card hero__card--float hero__card--2"><span class="hero__card-emoji">🐱</span><div><span class="hero__card-name">小橘</span><span class="hero__card-state">午睡中 💤</span></div></div>
            <div class="hero__card hero__card--float hero__card--3"><span class="hero__card-emoji">🐰</span><div><span class="hero__card-name">团子</span><span class="hero__card-state">进食中 🥕</span></div></div>
          </div>
        </div>
      </div>
    </section>
    <section class="stats-marquee"><div class="stats-marquee__track"><div class="stats-marquee__content"><span class="stat-item">✨ 500+ 认证机构</span><span class="stat-item">🐾 10万+ 服务宠物</span><span class="stat-item">⭐ 4.9 平均评分</span><span class="stat-item">💯 99% 好评率</span><span class="stat-item">🛡️ 全程保险保障</span><span class="stat-item">📱 实时状态追踪</span><span class="stat-item">✨ 500+ 认证机构</span><span class="stat-item">🐾 10万+ 服务宠物</span><span class="stat-item">⭐ 4.9 平均评分</span><span class="stat-item">💯 99% 好评率</span></div></div></section>
    <section class="quick-actions scroll-animate"><div class="container"><div class="quick-actions__grid"><button v-for="(link, index) in quickLinks" :key="link.name" class="quick-action scroll-animate-item" :style="{ '--delay': index * 0.1 + 's' }" @click="goToQuickLink(link)"><span class="quick-action__icon">{{ link.icon }}</span><span class="quick-action__name">{{ link.name }}</span><span class="quick-action__desc">{{ link.desc }}</span></button></div></div></section>
    <section class="services scroll-animate"><div class="container"><div class="section-header section-header--left scroll-animate-item"><span class="section-tag">🐾 服务类型</span><h2>为不同宠物<br/>提供专属方案</h2></div><div class="services__grid"><div v-for="(service, index) in serviceTypes" :key="service.name" class="service-card scroll-animate-item" :class="`service-card--${index + 1}`" :style="{ '--delay': index * 0.1 + 's' }"><div class="service-card__icon">{{ service.icon }}</div><div class="service-card__content"><h3>{{ service.name }}</h3><p>{{ service.desc }}</p></div><span class="service-card__emoji">{{ service.emoji }}</span><router-link to="/institutions" class="service-card__link">查看机构 →</router-link></div></div></div></section>
    <section class="process scroll-animate"><div class="container"><div class="section-header scroll-animate-item"><span class="section-tag">✨ 预约流程</span><h2>简单四步，轻松搞定</h2><p>从搜索到入住，全程线上操作</p></div><div class="process__timeline"><div v-for="(step, index) in processSteps" :key="index" class="process__step scroll-animate-item" :style="{ '--delay': index * 0.15 + 's' }"><div class="process__step-num" :style="{ background: step.color }">{{ index + 1 }}</div><div class="process__step-icon">{{ step.icon }}</div><h3>{{ step.title }}</h3><p>{{ step.desc }}</p></div><div class="process__line"></div></div></div></section>
    <section class="features scroll-animate"><div class="container"><div class="section-header scroll-animate-item"><span class="section-tag">💪 平台优势</span><h2>为什么选择我们</h2><p>专业、安全、透明，让您放心托付</p></div><div class="features__grid"><div class="feature-card scroll-animate-item" style="--delay: 0s"><span class="feature-card__icon">🛡️</span><h3>全程安全保障</h3><p>所有机构经过严格资质审核，平台为每只寄养宠物购买意外险</p></div><div class="feature-card scroll-animate-item" style="--delay: 0.1s"><span class="feature-card__icon">📱</span><h3>实时追踪</h3><p>每日推送照片视频，随时了解爱宠动态</p></div><div class="feature-card scroll-animate-item" style="--delay: 0.2s"><span class="feature-card__icon">💰</span><h3>价格透明</h3><p>所有费用明码标价，无隐藏收费</p></div><div class="feature-card scroll-animate-item" style="--delay: 0.3s"><span class="feature-card__icon">⭐</span><h3>真实评价</h3><p>只展示真实消费后的评价</p></div><div class="feature-card scroll-animate-item" style="--delay: 0.4s"><span class="feature-card__icon">🎯</span><h3>智能推荐</h3><p>根据需求智能匹配最合适的机构</p></div><div class="feature-card scroll-animate-item" style="--delay: 0.5s"><span class="feature-card__icon">🔔</span><h3>贴心提醒</h3><p>预约提醒、接宠通知一应俱全</p></div></div></div></section>
    <section class="featured scroll-animate"><div class="container"><div class="featured__header scroll-animate-item"><div class="section-header section-header--left"><span class="section-tag">🏆 精选推荐</span><h2>高评分寄养机构</h2></div><router-link to="/institutions" class="featured__more">查看全部 →</router-link></div><div v-if="loading" class="featured__grid"><InstitutionCardSkeleton v-for="i in 4" :key="i" /></div><div v-else class="featured__grid"><InstitutionCard v-for="(inst, index) in featuredInstitutions" :key="inst.id" :institution="inst" class="scroll-animate-item" :style="{ '--delay': index * 0.1 + 's' }" /></div></div></section>
    <section class="testimonials scroll-animate"><div class="container"><div class="section-header scroll-animate-item"><span class="section-tag">💬 用户评价</span><h2>听听他们怎么说</h2><p>真实用户的寄养体验分享</p></div><div class="testimonials__grid"><div v-for="(item, index) in testimonials" :key="item.id" class="testimonial scroll-animate-item" :style="{ '--bg-color': item.bgColor, '--delay': index * 0.15 + 's' }"><div class="testimonial__quote">"</div><p class="testimonial__content">{{ item.content }}</p><div class="testimonial__footer"><div class="testimonial__avatar">{{ item.avatar }}</div><div class="testimonial__info"><span class="testimonial__name">{{ item.name }}</span><span class="testimonial__pet">{{ item.pet }}</span></div><div class="testimonial__rating"><span v-for="i in item.rating" :key="i">⭐</span></div></div></div></div></div></section>
    <section class="faq scroll-animate"><div class="container"><div class="faq__layout"><div class="faq__left scroll-animate-item"><span class="section-tag">❓ 常见问题</span><h2>您可能想了解的</h2><p>关于寄养的常见疑问解答</p><div class="faq__cta"><router-link to="/help"><AppButton type="outline">访问帮助中心</AppButton></router-link></div></div><div class="faq__right"><div v-for="(faq, index) in faqs" :key="index" class="faq__item scroll-animate-item" :class="{ 'is-open': faq.open }" :style="{ '--delay': index * 0.1 + 's' }" @click="toggleFaq(index)"><div class="faq__question"><span>{{ faq.q }}</span><span class="faq__toggle">{{ faq.open ? '−' : '+' }}</span></div><div class="faq__answer" v-show="faq.open">{{ faq.a }}</div></div></div></div></div></section>
    <section v-if="!isAuthenticated" class="cta scroll-animate"><div class="cta__bg"><div class="cta__blob cta__blob--1"></div><div class="cta__blob cta__blob--2"></div></div><div class="container"><div class="cta__content scroll-animate-item"><span class="cta__icon">🐾</span><h2>准备好为爱宠找个好去处了吗？</h2><p>立即注册，开启安心寄养之旅</p><div class="cta__actions"><router-link to="/register"><AppButton type="primary" size="lg">免费注册</AppButton></router-link><router-link to="/institutions"><AppButton type="outline" size="lg">先看看机构</AppButton></router-link></div><div class="cta__trust"><span>🔒 信息安全</span><span>📞 7×24客服</span><span>💯 满意保证</span></div></div></div></section>
    <AIFloatingButton />
    <AIChatWindow />
  </div>
</template>

<style lang="scss" scoped>
.home-view {
  --cream: #FDF8F3;
  --peach: #FFE8D6;
  --peach-dark: #FFCBA4;
  --mint: #D4F5E9;
  --mint-dark: #7FD4C1;
  --lavender: #E8E0F0;
  --coral: #FF7F6B;
  --text-dark: #2D2A26;
  --text-muted: #6B6560;
  --text-light: #9A958F;
  margin: -24px;
  overflow-x: hidden;
  background: var(--cream);
}
.container { max-width: 1200px; margin: 0 auto; padding: 0 24px; }
.section-header { text-align: center; margin-bottom: 56px; }
.section-header--left { text-align: left; }
.section-header h2 { font-size: 40px; font-weight: 800; color: var(--text-dark); margin: 12px 0; line-height: 1.2; }
.section-header p { font-size: 17px; color: var(--text-muted); margin: 0; }
.section-tag { display: inline-block; padding: 8px 16px; background: white; border-radius: 100px; font-size: 14px; font-weight: 600; color: var(--text-dark); box-shadow: 0 2px 8px rgba(0,0,0,0.04); }

.hero { position: relative; min-height: 100vh; display: flex; align-items: center; padding: 80px 24px; overflow: hidden; }
.hero__blobs { position: absolute; inset: 0; pointer-events: none; }
.blob { position: absolute; border-radius: 50%; filter: blur(60px); opacity: 0.7; }
.blob--1 { width: 600px; height: 600px; background: var(--peach); top: -200px; right: -100px; animation: blobFloat 20s ease-in-out infinite; }
.blob--2 { width: 400px; height: 400px; background: var(--mint); bottom: -100px; left: -50px; animation: blobFloat 15s ease-in-out infinite reverse; }
.blob--3 { width: 300px; height: 300px; background: var(--lavender); top: 40%; left: 30%; animation: blobFloat 18s ease-in-out infinite 2s; }
.hero__noise { position: absolute; inset: 0; background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E"); opacity: 0.03; pointer-events: none; }
.hero__content { position: relative; z-index: 1; max-width: 1200px; margin: 0 auto; display: grid; grid-template-columns: 1fr 1fr; gap: 60px; align-items: center; }
.hero__left { animation: fadeInUp 0.6s ease; }
.hero__tag { display: inline-flex; align-items: center; gap: 8px; padding: 10px 20px; background: white; border-radius: 100px; font-size: 14px; font-weight: 600; color: var(--text-dark); box-shadow: 0 4px 20px rgba(0,0,0,0.06); margin-bottom: 32px; }
.hero__tag-dot { width: 8px; height: 8px; background: var(--coral); border-radius: 50%; animation: pulse 2s ease-in-out infinite; }
.hero__title { font-size: 52px; font-weight: 800; line-height: 1.15; color: var(--text-dark); margin: 0 0 24px; }
.hero__title-highlight { position: relative; display: inline-block; color: var(--coral); }
.hero__title-underline { position: absolute; bottom: -4px; left: 0; width: 100%; height: 12px; color: var(--peach-dark); opacity: 0.6; }
.hero__desc { font-size: 18px; line-height: 1.8; color: var(--text-muted); margin: 0 0 40px; }
.hero__search-inner { display: flex; align-items: center; gap: 12px; padding: 8px 8px 8px 24px; background: white; border-radius: 100px; box-shadow: 0 8px 40px rgba(0,0,0,0.08); }
.hero__search-icon { font-size: 20px; opacity: 0.5; }
.hero__search-inner input { flex: 1; padding: 14px 0; border: none; font-size: 16px; background: transparent; color: var(--text-dark); outline: none; }
.hero__search-btn { padding: 14px 32px; background: linear-gradient(135deg, var(--coral) 0%, #FF9F7F 100%); color: white; border: none; border-radius: 100px; font-size: 15px; font-weight: 600; cursor: pointer; transition: all 0.3s ease; }
.hero__search-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(255, 127, 107, 0.4); }
.hero__search-tags { display: flex; align-items: center; gap: 12px; margin-top: 16px; flex-wrap: wrap; }
.hero__search-tags span { font-size: 13px; color: var(--text-light); }
.hero__search-tags a { padding: 6px 14px; background: rgba(255,255,255,0.7); border-radius: 100px; font-size: 13px; color: var(--text-muted); transition: all 0.2s; }
.hero__search-tags a:hover { background: white; color: var(--coral); }
.hero__right { position: relative; height: 500px; }
.hero__cards { position: relative; width: 100%; height: 100%; }
.hero__card { position: absolute; background: white; border-radius: 20px; box-shadow: 0 12px 40px rgba(0,0,0,0.08); }
.hero__card--main { top: 50%; left: 50%; transform: translate(-50%, -50%); padding: 32px; display: flex; align-items: center; gap: 20px; animation: floatMainCard 6s ease-in-out infinite; }
@keyframes floatMainCard { 0%, 100% { transform: translate(-50%, -50%); } 50% { transform: translate(-50%, calc(-50% - 10px)); } }
.hero__card-avatar { width: 80px; height: 80px; background: linear-gradient(135deg, var(--peach) 0%, var(--mint) 100%); border-radius: 20px; display: flex; align-items: center; justify-content: center; font-size: 40px; }
.hero__card-label { font-size: 18px; font-weight: 700; color: var(--text-dark); display: block; }
.hero__card-status { display: flex; align-items: center; gap: 6px; font-size: 14px; color: var(--text-muted); margin-top: 4px; }
.hero__card--float { display: flex; align-items: center; gap: 12px; padding: 16px 20px; }
.hero__card-emoji { font-size: 32px; }
.hero__card-name { font-weight: 600; font-size: 14px; color: var(--text-dark); display: block; }
.hero__card-state { font-size: 12px; color: var(--text-muted); }
.hero__card--1 { top: 10%; left: 5%; animation: floatCard1 4s ease-in-out infinite; }
.hero__card--2 { top: 25%; right: 0; animation: floatCard2 4.5s ease-in-out infinite; }
.hero__card--3 { bottom: 15%; left: 15%; animation: floatCard3 5s ease-in-out infinite; }
@keyframes floatCard1 { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-15px); } }
@keyframes floatCard2 { 0%, 100% { transform: translateY(0) translateX(0); } 50% { transform: translateY(-12px) translateX(-5px); } }
@keyframes floatCard3 { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-18px); } }
.status-dot { width: 8px; height: 8px; border-radius: 50%; background: #ccc; }
.status-dot--active { background: #22C55E; animation: pulse 2s ease-in-out infinite; }

.stats-marquee { background: linear-gradient(135deg, var(--coral) 0%, #FF9F7F 100%); padding: 20px 0; overflow: hidden; }
.stats-marquee__track { display: flex; width: max-content; }
.stats-marquee__content { display: flex; gap: 48px; animation: marquee 30s linear infinite; }
.stat-item { font-size: 15px; font-weight: 500; color: white; white-space: nowrap; }

.quick-actions { padding: 80px 24px; background: white; }
.quick-actions__grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.quick-action { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 32px 24px; background: var(--cream); border: 2px solid transparent; border-radius: 24px; cursor: pointer; transition: all 0.3s ease; text-align: center; }
.quick-action:hover { transform: translateY(-8px); border-color: var(--coral); box-shadow: 0 20px 40px rgba(255, 127, 107, 0.15); }
.quick-action__icon { font-size: 40px; transition: transform 0.3s ease; }
.quick-action:hover .quick-action__icon { transform: scale(1.1) rotate(5deg); }
.quick-action__name { font-size: 16px; font-weight: 700; color: var(--text-dark); }
.quick-action__desc { font-size: 13px; color: var(--text-muted); }

.services { padding: 100px 24px; background: var(--cream); }
.services__grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; }
.service-card { position: relative; background: white; border-radius: 28px; padding: 36px 28px; overflow: hidden; transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1); }
.service-card:hover { transform: translateY(-12px) rotate(-1deg); box-shadow: 0 24px 48px rgba(0,0,0,0.1); }
.service-card--1 { background: linear-gradient(180deg, #FFF5F0 0%, white 100%); }
.service-card--2 { background: linear-gradient(180deg, #F0FFF5 0%, white 100%); }
.service-card--3 { background: linear-gradient(180deg, #FFF5FF 0%, white 100%); }
.service-card--4 { background: linear-gradient(180deg, #F0F5FF 0%, white 100%); }
.service-card__icon { font-size: 56px; margin-bottom: 20px; }
.service-card__content h3 { font-size: 20px; font-weight: 700; color: var(--text-dark); margin: 0 0 8px; }
.service-card__content p { font-size: 14px; color: var(--text-muted); margin: 0; line-height: 1.6; }
.service-card__emoji { position: absolute; top: 20px; right: 20px; font-size: 32px; opacity: 0.15; transition: all 0.4s ease; }
.service-card:hover .service-card__emoji { transform: scale(1.2) rotate(15deg); opacity: 0.3; }
.service-card__link { display: inline-block; margin-top: 20px; font-size: 14px; font-weight: 600; color: var(--coral); transition: all 0.2s; }
.service-card__link:hover { transform: translateX(4px); }

.process { padding: 100px 24px; background: white; }
.process__timeline { position: relative; display: grid; grid-template-columns: repeat(4, 1fr); gap: 32px; }
.process__line { position: absolute; top: 80px; left: 12.5%; right: 12.5%; height: 4px; background: linear-gradient(90deg, var(--peach), var(--mint), var(--lavender), var(--peach)); border-radius: 2px; }
.process__step { position: relative; text-align: center; z-index: 1; }
.process__step-num { position: absolute; top: 0; right: calc(50% - 60px); width: 28px; height: 28px; color: white; font-size: 13px; font-weight: 700; border-radius: 50%; display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.process__step-icon { width: 120px; height: 120px; margin: 0 auto 24px; background: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 48px; box-shadow: 0 8px 32px rgba(0,0,0,0.06); transition: transform 0.3s ease; }
.process__step:hover .process__step-icon { transform: scale(1.1); }
.process__step h3 { font-size: 20px; font-weight: 700; color: var(--text-dark); margin: 0 0 8px; }
.process__step p { font-size: 14px; color: var(--text-muted); margin: 0; }
</style>

<style lang="scss" scoped>
.features { padding: 100px 24px; background: white; }
.features__grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; }
.feature-card { background: var(--cream); border-radius: 28px; padding: 36px 32px; text-align: center; transition: all 0.3s ease; border: 2px solid transparent; }
.feature-card:hover { transform: translateY(-8px); box-shadow: 0 20px 40px rgba(0,0,0,0.08); border-color: var(--peach); }
.feature-card__icon { font-size: 56px; margin-bottom: 20px; display: block; }
.feature-card h3 { font-size: 20px; font-weight: 700; color: var(--text-dark); margin: 0 0 12px; }
.feature-card p { font-size: 14px; color: var(--text-muted); margin: 0; line-height: 1.7; }

.featured { padding: 100px 24px; background: var(--cream); }
.featured__header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 48px; }
.featured__more { font-size: 15px; font-weight: 600; color: var(--coral); transition: all 0.2s; }
.featured__more:hover { transform: translateX(4px); }
.featured__grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 24px; }

.testimonials { padding: 100px 24px; background: var(--cream); }
.testimonials__grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; }
.testimonial { background: var(--bg-color, white); border-radius: 28px; padding: 32px; position: relative; transition: all 0.3s ease; }
.testimonial:hover { transform: translateY(-8px) rotate(1deg); box-shadow: 0 20px 40px rgba(0,0,0,0.08); }
.testimonial__quote { position: absolute; top: 20px; right: 28px; font-family: Georgia, serif; font-size: 80px; color: var(--coral); opacity: 0.15; line-height: 1; }
.testimonial__content { font-size: 15px; line-height: 1.8; color: var(--text-dark); margin: 0 0 24px; position: relative; z-index: 1; }
.testimonial__footer { display: flex; align-items: center; gap: 12px; }
.testimonial__avatar { width: 48px; height: 48px; background: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24px; box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
.testimonial__info { flex: 1; }
.testimonial__name { font-weight: 700; font-size: 15px; color: var(--text-dark); display: block; }
.testimonial__pet { font-size: 13px; color: var(--text-muted); }
.testimonial__rating { font-size: 12px; }

.faq { padding: 100px 24px; background: white; }
.faq__layout { display: grid; grid-template-columns: 1fr 2fr; gap: 60px; align-items: start; }
.faq__left { position: sticky; top: 100px; }
.faq__left h2 { font-size: 36px; font-weight: 800; color: var(--text-dark); margin: 16px 0; line-height: 1.2; }
.faq__left p { font-size: 16px; color: var(--text-muted); margin: 0 0 32px; }
.faq__item { background: var(--cream); border-radius: 20px; margin-bottom: 12px; overflow: hidden; cursor: pointer; transition: all 0.3s ease; }
.faq__item:hover { background: var(--peach); }
.faq__item.is-open { background: var(--peach); }
.faq__question { display: flex; justify-content: space-between; align-items: center; padding: 24px 28px; font-weight: 600; font-size: 16px; color: var(--text-dark); }
.faq__toggle { width: 32px; height: 32px; background: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; color: var(--coral); flex-shrink: 0; }
.faq__answer { padding: 0 28px 24px; font-size: 15px; line-height: 1.8; color: var(--text-muted); }

.cta { position: relative; padding: 120px 24px; background: linear-gradient(135deg, var(--coral) 0%, #FF9F7F 100%); overflow: hidden; }
.cta__bg { position: absolute; inset: 0; pointer-events: none; }
.cta__blob { position: absolute; border-radius: 50%; }
.cta__blob--1 { width: 400px; height: 400px; background: rgba(255,255,255,0.1); top: -100px; left: -100px; }
.cta__blob--2 { width: 300px; height: 300px; background: rgba(255,255,255,0.1); bottom: -50px; right: -50px; }
.cta__content { position: relative; z-index: 1; text-align: center; max-width: 600px; margin: 0 auto; color: white; }
.cta__icon { font-size: 64px; margin-bottom: 24px; display: block; }
.cta h2 { font-size: 40px; font-weight: 800; margin: 0 0 12px; }
.cta p { font-size: 18px; opacity: 0.9; margin: 0 0 40px; }
.cta__actions { display: flex; gap: 16px; justify-content: center; margin-bottom: 32px; }
.cta__actions :deep(.app-button--primary) { background: white; color: var(--coral); }
.cta__actions :deep(.app-button--primary:hover) { background: #f5f5f5; }
.cta__actions :deep(.app-button--outline) { border-color: white; color: white; }
.cta__actions :deep(.app-button--outline:hover) { background: rgba(255, 255, 255, 0.1); }
.cta__trust { display: flex; justify-content: center; gap: 32px; flex-wrap: wrap; }
.cta__trust span { font-size: 14px; opacity: 0.85; }

@keyframes fadeInUp { from { opacity: 0; transform: translateY(24px); } to { opacity: 1; transform: translateY(0); } }
@keyframes fadeInLeft { from { opacity: 0; transform: translateX(-30px); } to { opacity: 1; transform: translateX(0); } }
@keyframes fadeInRight { from { opacity: 0; transform: translateX(30px); } to { opacity: 1; transform: translateX(0); } }
@keyframes scaleIn { from { opacity: 0; transform: scale(0.9); } to { opacity: 1; transform: scale(1); } }
@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-12px); } }
@keyframes floatY { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-15px); } }
@keyframes floatMain { 0%, 100% { transform: translate(-50%, -50%); } 50% { transform: translate(-50%, calc(-50% - 10px)); } }
@keyframes mainCardEnter { from { opacity: 0; transform: translate(-50%, -40%) scale(0.9); } to { opacity: 1; transform: translate(-50%, -50%) scale(1); } }
@keyframes cardEnter1 { from { opacity: 0; transform: translateX(-50px) translateY(20px); } to { opacity: 1; transform: translateX(0) translateY(0); } }
@keyframes cardEnter2 { from { opacity: 0; transform: translateX(50px) translateY(-20px); } to { opacity: 1; transform: translateX(0) translateY(0); } }
@keyframes cardEnter3 { from { opacity: 0; transform: translateY(50px) scale(0.8); } to { opacity: 1; transform: translateY(0) scale(1); } }
@keyframes blobFloat { 0%, 100% { transform: translate(0, 0) scale(1); } 33% { transform: translate(30px, -30px) scale(1.05); } 66% { transform: translate(-20px, 20px) scale(0.95); } }
@keyframes pulse { 0%, 100% { opacity: 1; transform: scale(1); } 50% { opacity: 0.5; transform: scale(1.2); } }
@keyframes marquee { 0% { transform: translateX(0); } 100% { transform: translateX(-50%); } }

// 滚动动画基础样式
.scroll-animate-item {
  opacity: 0;
  transform: translateY(40px);
  transition: opacity 0.6s cubic-bezier(0.4, 0, 0.2, 1), 
              transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  transition-delay: var(--delay, 0s);
}

.scroll-animate.animate-in .scroll-animate-item,
.scroll-animate-item.animate-in {
  opacity: 1;
  transform: translateY(0);
}

// 特殊动画效果
.quick-action.scroll-animate-item {
  transform: translateY(30px) scale(0.95);
}
.scroll-animate.animate-in .quick-action.scroll-animate-item {
  transform: translateY(0) scale(1);
}

.service-card.scroll-animate-item {
  transform: translateY(50px) rotate(2deg);
}
.scroll-animate.animate-in .service-card.scroll-animate-item {
  transform: translateY(0) rotate(0deg);
}

.process__step.scroll-animate-item {
  transform: translateY(40px) scale(0.9);
}
.scroll-animate.animate-in .process__step.scroll-animate-item {
  transform: translateY(0) scale(1);
}

.feature-card.scroll-animate-item {
  transform: translateY(30px);
}
.scroll-animate.animate-in .feature-card.scroll-animate-item {
  transform: translateY(0);
}

.testimonial.scroll-animate-item {
  transform: translateY(40px) rotate(-1deg);
}
.scroll-animate.animate-in .testimonial.scroll-animate-item {
  transform: translateY(0) rotate(0deg);
}

.faq__item.scroll-animate-item {
  transform: translateX(-20px);
}
.scroll-animate.animate-in .faq__item.scroll-animate-item {
  transform: translateX(0);
}

.cta__content.scroll-animate-item {
  transform: translateY(50px) scale(0.95);
}
.scroll-animate.animate-in .cta__content.scroll-animate-item {
  transform: translateY(0) scale(1);
}

@media (max-width: 1024px) {
  .hero { min-height: auto; padding: 60px 24px; }
  .hero__content { grid-template-columns: 1fr; text-align: center; }
  .hero__right { display: none; }
  .hero__search-tags { justify-content: center; }
  .quick-actions__grid { grid-template-columns: repeat(2, 1fr); }
  .services__grid { grid-template-columns: repeat(2, 1fr); }
  .features__grid { grid-template-columns: repeat(2, 1fr); }
  .featured__grid { grid-template-columns: repeat(2, 1fr); }
  .testimonials__grid { grid-template-columns: repeat(2, 1fr); }
  .faq__layout { grid-template-columns: 1fr; gap: 40px; }
  .faq__left { position: static; text-align: center; }
  .faq__cta { display: none; }
}
@media (max-width: 768px) {
  .hero__title { font-size: 36px; }
  .process__timeline { grid-template-columns: 1fr; gap: 48px; }
  .process__line { display: none; }
  .cta h2 { font-size: 28px; }
  .app-download__card { flex-direction: column; text-align: center; padding: 40px 32px; }
  .app-download__features { justify-content: center; }
}
@media (max-width: 640px) {
  .hero__search-inner { flex-direction: column; border-radius: 24px; padding: 16px; }
  .hero__search-inner input { width: 100%; text-align: center; }
  .hero__search-btn { width: 100%; }
  .quick-actions__grid { grid-template-columns: 1fr; }
  .services__grid { grid-template-columns: 1fr; }
  .features__grid { grid-template-columns: 1fr; }
  .featured__grid { grid-template-columns: 1fr; }
  .featured__header { flex-direction: column; align-items: flex-start; gap: 16px; }
  .testimonials__grid { grid-template-columns: 1fr; }
  .cta__actions { flex-direction: column; }
}
</style>
