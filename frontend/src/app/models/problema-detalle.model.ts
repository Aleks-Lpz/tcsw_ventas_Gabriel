import { HttpErrorResponse } from '@angular/common/http';

export interface ProblemaDetalle {
  type: string;
  title: string;
  status: number;
  detail: string;
}

/**
 * Extrae el mensaje legible desde ProblemaDetalle (error.error?.detail)
 * sin exponer trazas técnicas al usuario final (Saber Axiológico M13).
 */
export function extraerMensajeError(
  error: HttpErrorResponse,
  mensajePorDefecto = 'Ocurrió un error al comunicarse con el servidor.'
): string {
  const detalle = (error?.error as Partial<ProblemaDetalle> | undefined)?.detail;
  if (typeof detalle === 'string' && detalle.trim().length > 0) {
    return detalle.trim();
  }

  const titulo = (error?.error as Partial<ProblemaDetalle> | undefined)?.title;
  if (typeof titulo === 'string' && titulo.trim().length > 0) {
    return titulo.trim();
  }

  if (error.status === 0) {
    return 'No se pudo conectar con la API local (http://localhost:8080). Verifique que el servidor esté activo.';
  }

  return mensajePorDefecto;
}