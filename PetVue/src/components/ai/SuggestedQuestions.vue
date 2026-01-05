<template>
  <div class="suggested-questions">
    <p class="section-label">您可以问我：</p>
    <div class="questions-list">
      <button
        v-for="question in questions"
        :key="question.id"
        class="question-btn"
        @click="$emit('select', question)"
      >
        <component :is="getCategoryIcon(question.category)" :size="16" />
        <span>{{ question.text }}</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { MapPin, Calendar, Heart, CreditCard, HelpCircle } from 'lucide-vue-next'
import type { SuggestedQuestion, QuestionCategory } from '@/types/ai'

defineProps<{
  questions: SuggestedQuestion[]
}>()

defineEmits<{
  (e: 'select', question: SuggestedQuestion): void
}>()

function getCategoryIcon(category: QuestionCategory) {
  const icons = {
    institution: MapPin,
    booking: Calendar,
    pet_care: Heart,
    payment: CreditCard,
    general: HelpCircle
  }
  return icons[category] || HelpCircle
}
</script>

<style scoped lang="scss">
.suggested-questions {
  text-align: left;
}

.section-label {
  font-size: 12px;
  color: var(--color-text-muted);
  margin: 0 0 12px;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.question-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 12px 14px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-primary);
  font-size: 13px;
  text-align: left;
  cursor: pointer;
  transition: all 0.2s;

  svg {
    flex-shrink: 0;
    color: var(--color-primary);
  }

  &:hover {
    border-color: var(--color-primary);
    background: var(--color-primary-light);
  }
}
</style>
