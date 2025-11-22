import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ExportacionesComponent } from './exportaciones';

describe('ExportacionesComponent', () => {
  let component: ExportacionesComponent;
  let fixture: ComponentFixture<ExportacionesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExportacionesComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(ExportacionesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
