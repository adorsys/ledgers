import { Component, OnInit } from '@angular/core';
import {User} from "../../../models/user.model";
import {FormArray, FormBuilder, FormGroup, Validators} from "@angular/forms";
import {UserService} from "../../../services/user.service";
import {Router} from "@angular/router";

@Component({
  selector: 'edit-sca',
  templateUrl: './edit-sca.component.html',
  styleUrls: ['./edit-sca.component.css']
})
export class EditScaComponent implements OnInit {

  id: string;
  user: User;

  scaForm: FormGroup;

  constructor(
    private userService: UserService,
    private formBuilder: FormBuilder,
    private router: Router) { }

  ngOnInit() {
    this.scaForm = this.formBuilder.group({
      scaUserData: this.formBuilder.array([
        this.initScaData(),
      ])
    });
  }

  initScaData() {
    return this.formBuilder.group({
      scaMethod: ['', Validators.required],
      methodValue: ['', Validators.required]
    })
  }

  addScaDataItem() {
    const control = <FormArray>this.scaForm.controls['scaUserData'];
    control.push(this.initScaData());
  }

  removeScaDataItem(i: number) {
    const control = <FormArray>this.scaForm.controls['scaUserData'];
    control.removeAt(i);
  }


  onSubmit() {
    console.log(this.scaForm.value);
    // this.userService.createUser(this.userForm.value)
    //   .subscribe(response => {
    //     this.router.navigateByUrl('/users');
    //   });
  }

}
