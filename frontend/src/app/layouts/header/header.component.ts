import {Component, OnInit} from '@angular/core';
import { Observable } from "rxjs";
import {SharedService} from "../../shared/shared.service";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})

export class HeaderComponent implements OnInit {
  userName$?: Observable<string>;
  teamName$?: Observable<string>;
  totalPoints$?: Observable<number>;
  overallRank$?: Observable<number>;

  constructor(protected sharedService: SharedService) {
  }

  ngOnInit(): void {
    this.userName$ = this.sharedService.userName.asObservable();
    this.teamName$ = this.sharedService.teamName.asObservable();
    this.totalPoints$ = this.sharedService.totalPoints.asObservable();
    this.overallRank$ = this.sharedService.overallRank.asObservable();
  }



}
