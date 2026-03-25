import vue from 'eslint-plugin-vue'

export default [
  ...vue.configs['flat/recommended'],
  {
    files: ['src/**/*.{js,vue}'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module'
    },
    rules: {
      'no-console': 'off',
      'vue/multi-word-component-names': 'off'
    }
  }
]
