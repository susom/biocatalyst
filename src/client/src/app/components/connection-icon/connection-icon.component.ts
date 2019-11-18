import { Component, Input, OnChanges } from '@angular/core';

export enum ConnectionState {
  DISCONNECTED,
  LOADING,
  CONNECTED
}

@Component({
  selector: 'app-connection-icon',
  templateUrl: './connection-icon.component.html',
  styleUrls: ['./connection-icon.component.scss']
})
export class ConnectionIconComponent implements OnChanges {
  @Input() connected = false;
  @Input() loading = false;

  public states: typeof ConnectionState;
  private currentState: ConnectionState;

  constructor() {
    this.states = ConnectionState;
    this.currentState = ConnectionState.DISCONNECTED;
  }

  ngOnChanges() {
    this.currentState = (this.connected) ? ConnectionState.CONNECTED : ConnectionState.DISCONNECTED;
    this.currentState = (this.loading) ? ConnectionState.LOADING : this.currentState;
  }
}
