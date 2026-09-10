import { Component } from '@angular/core';

/**
 * Footer layout component displaying copyright information.
 */
@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent {
  /** The current year, used for the copyright notice. */
  readonly currentYear = new Date().getFullYear();
}
