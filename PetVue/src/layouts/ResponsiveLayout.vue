<script setup lang="ts">
import { useDevice } from '@/composables/useDevice'
import MainLayout from './MainLayout.vue'
import MobileLayout from './MobileLayout.vue'

const { isMobile } = useDevice()
</script>

<template>
  <component :is="isMobile ? MobileLayout : MainLayout">
    <slot></slot>
  </component>
</template>
