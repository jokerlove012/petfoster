<script setup lang="ts">
import { computed, ref } from 'vue'

interface Props {
  modelValue?: string | number
  type?: 'text' | 'password' | 'email' | 'number' | 'tel' | 'search'
  placeholder?: string
  label?: string
  error?: string
  hint?: string
  disabled?: boolean
  readonly?: boolean
  clearable?: boolean
  size?: 'sm' | 'md' | 'lg'
  prefixIcon?: boolean
  suffixIcon?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  placeholder: '',
  disabled: false,
  readonly: false,
  clearable: false,
  size: 'md'
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  focus: [event: FocusEvent]
  blur: [event: FocusEvent]
  clear: []
}>()

const isFocused = ref(false)
const showPassword = ref(false)

const inputType = computed(() => {
  if (props.type === 'password') {
    return showPassword.value ? 'text' : 'password'
  }
  return props.type
})

const wrapperClass = computed(() => [
  'app-input',
  `app-input--${props.size}`,
  {
    'app-input--focused': isFocused.value,
    'app-input--error': props.error,
    'app-input--disabled': props.disabled,
    'app-input--has-prefix': props.prefixIcon,
    'app-input--has-suffix': props.suffixIcon || props.clearable || props.type === 'password'
  }
])

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.value)
}

const handleFocus = (event: FocusEvent) => {
  isFocused.value = true
  emit('focus', event)
}

const handleBlur = (event: FocusEvent) => {
  isFocused.value = false
  emit('blur', event)
}

const handleClear = () => {
  emit('update:modelValue', '')
  emit('clear')
}

const togglePassword = () => {
  showPassword.value = !showPassword.value
}
</script>

<template>
  <div class="app-input-wrapper">
    <label v-if="label" class="app-input__label">{{ label }}</label>
    <div :class="wrapperClass">
      <span v-if="prefixIcon" class="app-input__prefix">
        <slot name="prefix"></slot>
      </span>
      <input
        :type="inputType"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        class="app-input__field"
        @input="handleInput"
        @focus="handleFocus"
        @blur="handleBlur"
      />
      <span v-if="clearable && modelValue" class="app-input__suffix app-input__clear" @click="handleClear">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18"></line>
          <line x1="6" y1="6" x2="18" y2="18"></line>
        </svg>
      </span>
      <span v-if="type === 'password'" class="app-input__suffix app-input__toggle" @click="togglePassword">
        <svg v-if="showPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
          <line x1="1" y1="1" x2="23" y2="23"></line>
        </svg>
        <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
          <circle cx="12" cy="12" r="3"></circle>
        </svg>
      </span>
      <span v-if="suffixIcon" class="app-input__suffix">
        <slot name="suffix"></slot>
      </span>
    </div>
    <p v-if="error" class="app-input__error">{{ error }}</p>
    <p v-else-if="hint" class="app-input__hint">{{ hint }}</p>
  </div>
</template>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.app-input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.app-input {
  display: flex;
  align-items: center;
  background: var(--color-surface);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  transition: all 150ms ease;
  overflow: hidden;

  // Sizes
  &--sm {
    .app-input__field {
      padding: 8px 12px;
      font-size: $text-sm;
    }
  }

  &--md {
    .app-input__field {
      padding: 12px 16px;
      font-size: $text-base;
    }
  }

  &--lg {
    .app-input__field {
      padding: 16px 20px;
      font-size: $text-lg;
    }
  }

  // States
  &:hover:not(&--disabled) {
    border-color: var(--color-primary);
  }

  &--focused {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 4px rgba(255, 107, 53, 0.1);
  }

  &--error {
    border-color: var(--color-error);

    &.app-input--focused {
      box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.1);
    }
  }

  &--disabled {
    background: $neutral-100;
    cursor: not-allowed;

    .app-input__field {
      cursor: not-allowed;
      color: var(--color-text-muted);
    }
  }

  &--has-prefix {
    .app-input__field {
      padding-left: 8px;
    }
  }

  &--has-suffix {
    .app-input__field {
      padding-right: 8px;
    }
  }

  // Field
  &__field {
    flex: 1;
    border: none;
    outline: none;
    background: transparent;
    font-family: var(--font-body);
    color: var(--color-text-primary);
    width: 100%;

    &::placeholder {
      color: var(--color-text-muted);
    }
  }

  // Prefix & Suffix
  &__prefix,
  &__suffix {
    display: flex;
    align-items: center;
    color: var(--color-text-muted);
    padding: 0 12px;
  }

  &__clear,
  &__toggle {
    cursor: pointer;
    transition: color 150ms ease;

    &:hover {
      color: var(--color-text-primary);
    }
  }

  // Label
  &__label {
    font-size: $text-sm;
    font-weight: 500;
    color: var(--color-text-secondary);
  }

  // Error & Hint
  &__error {
    font-size: $text-sm;
    color: var(--color-error);
    margin: 0;
  }

  &__hint {
    font-size: $text-sm;
    color: var(--color-text-muted);
    margin: 0;
  }
}
</style>
