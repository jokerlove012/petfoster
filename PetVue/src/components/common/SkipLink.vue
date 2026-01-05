<template>
  <a href="#main-content" class="skip-link" @click="skipToMain">
    跳转到主要内容
  </a>
</template>

<script setup lang="ts">
function skipToMain(e: Event) {
  e.preventDefault()
  const main = document.getElementById('main-content')
  if (main) {
    main.focus()
    main.scrollIntoView({ behavior: 'smooth' })
  }
}
</script>

<style scoped lang="scss">
.skip-link {
  position: fixed;
  top: -100%;
  left: 16px;
  z-index: 9999;
  padding: 12px 24px;
  background: var(--color-primary);
  color: white;
  font-weight: 600;
  text-decoration: none;
  border-radius: var(--radius-md);
  transition: top 0.2s;

  &:focus {
    top: 16px;
    outline: 2px solid var(--color-primary);
    outline-offset: 2px;
  }
}
</style>
