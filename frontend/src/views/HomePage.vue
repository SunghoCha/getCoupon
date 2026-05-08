<script>
export default {
  name: 'HomePage',
  data() {
    return {
      message: 'letterPick — 흩어진 뉴스레터를 한곳에서.',
    }
  },
}
</script>

<template>
  <v-container class="text-center py-12">
    <h1 class="text-h2 mb-4">letterPick</h1>
    <p class="text-body-1 text-medium-emphasis">{{ message }}</p>
  </v-container>
</template>
