/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserNewComponent } from './user-new.component';

describe('UserComponent', () => {
  let component: UserNewComponent;
  let fixture: ComponentFixture<UserNewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UserNewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserNewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
