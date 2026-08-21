import {Routes} from '@angular/router';
import {Access} from './access';
import {Error} from './error';
import {ForgotPassword} from './forgot-password';
import {ResetPassword} from './reset-password';
import {VincularSocio} from './vincular-socio';
import {LoginComponent} from "@/pages/auth/login/login.component";
import {RegisterComponent} from "@/pages/auth/register/register.component";
import {CompleteProfileComponent} from "@/pages/auth/complete-profile/complete-profile.component";

export default [
    {path: 'access', component: Access},
    {path: 'error', component: Error},
    {path: 'login', component: LoginComponent},
    {path: 'forgot-password', component: ForgotPassword},
    {path: 'reset-password', component: ResetPassword},
    {path: 'vincular-socio', component: VincularSocio},
    {path: 'register', component: RegisterComponent},
    {path: 'complete-profile', component: CompleteProfileComponent}
] as Routes;
