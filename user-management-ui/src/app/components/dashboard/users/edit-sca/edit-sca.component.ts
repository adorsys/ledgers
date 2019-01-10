import { Component, OnInit } from '@angular/core';
import {User} from "../../../../models/user.model";
import {FormArray, FormBuilder, FormGroup, Validators} from "@angular/forms";
import {UserService} from "../../../../services/user.service";
import {ActivatedRoute, Router} from "@angular/router";

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
    private route: ActivatedRoute,
    private router: Router) { }

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.id = params['id'];

      this.scaForm = this.formBuilder.group({
        scaUserData: this.formBuilder.array([
          this.initScaData(),
        ])
      });
    });
  }

  initScaData() {
    return this.formBuilder.group({
      scaMethod: ['', Validators.required],
      methodValue: ['', Validators.required]
    })
  }

  loadScaData() {
    this.userService.getUserById(this.id)
      .subscribe((user: User) => {
        this.user = user;
        console.log(user);
        if (this.user.scaUserData.length == 0) {
          return this.initScaData();
        } else {
          const control = <FormArray>this.scaForm.controls['scaUserData'];

          for (let i = 0; i < this.user.scaUserData.length; i++) {
            control.push(
              this.formBuilder.group({
                scaMethod: [this.user.scaUserData[i].scaMethod, Validators.required],
                methodValue: [this.user.scaUserData[i].methodValue, Validators.required]
              })
            );
          }

          return control;
        }
      });
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
    if (this.scaForm.invalid) {
      return;
    }

    this.userService.updateScaData(this.id, this.scaForm.controls['scaUserData'].value)
      .subscribe(response => {
        console.log(response);
        //this.router.navigate(['/users']);
      });

  }

}
