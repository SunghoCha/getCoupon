<script>
export default {
  name: 'NotFoundPage',
}
</script>

<template>
  <v-container class="text-center py-16">
    <h1 class="text-h1 mb-2">404</h1>
    <p class="text-body-1 text-medium-emphasis mb-6">존재하지 않는 페이지입니다.</p>
    <v-btn :to="{ name: 'home' }" color="primary">홈으로 돌아가기</v-btn>
  </v-container>
</template>
