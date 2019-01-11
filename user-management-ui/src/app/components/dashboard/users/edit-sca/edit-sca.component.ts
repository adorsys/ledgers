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
  scaArray: FormArray;

  constructor(
    private userService: UserService,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router) { }

  ngOnInit() {
    // create initial form
    this.createScaForm();

    this.route.params
      .subscribe(params => {
        // loading user ID
        this.id = params['id'];

        // loading user
        this.userService.getUserById(this.id)
          .subscribe((user: User) => {
            this.user = user;

            this.scaForm = new FormGroup({
              scaUserData: new FormArray(this.user.scaUserData.map(item => {
                const group = this.initScaData();
                group.patchValue(item);
                return group;
              }))
            });
          });
      });
  }

  createScaForm() {
    this.scaForm = this.formBuilder.group({
      scaUserData: this.formBuilder.array([
        this.scaArray
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
    if (this.scaForm.invalid) {
      return;
    }

    this.userService.updateScaData(this.id, this.scaForm.controls['scaUserData'].value)
      .subscribe(() => {
        this.router.navigate(['/users/' + this.id]);
      });

  }

}
