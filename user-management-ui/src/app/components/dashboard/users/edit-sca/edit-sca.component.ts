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

    this.userService.updateScaData(this.id, this.scaForm.value)
      .subscribe(response => {
        console.log(response);
        //this.router.navigate(['/users']);
      });

  }

}
