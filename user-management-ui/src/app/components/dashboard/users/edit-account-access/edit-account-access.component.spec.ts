/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EditAccountAccessComponent } from './edit-account-access.component';

describe('EditAccountAccessComponent', () => {
  let component: EditAccountAccessComponent;
  let fixture: ComponentFixture<EditAccountAccessComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditAccountAccessComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditAccountAccessComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
