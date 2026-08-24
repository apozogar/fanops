import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '@/pages/auth/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  // Al login DE LA PEÑA por la que se intentaba entrar: el dominio está en la propia URL, así
  // que quien llega con la sesión caducada se encuentra el login de su peña y no el genérico.
  const slug = route.paramMap.get('penaSlug');
  router.navigate(slug ? ['/', slug, 'auth', 'login'] : ['/auth/login']);
  return false;
};
