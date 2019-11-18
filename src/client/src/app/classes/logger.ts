import { Log } from 'ng2-logger/browser';
import { sprintf } from 'sprintf-js';

import { environment } from '../../environments/environment';

export function Deprecated(): MethodDecorator {
  return (target: any, propertyKey: string, descriptor: PropertyDescriptor) => {
    Logger.console('This function has been deprecated!', [target, propertyKey, descriptor]);
    return descriptor;
  };
}

export function Track(Category: string): MethodDecorator {
  return (target: any, propertyKey: string, descriptor: PropertyDescriptor) => {
    Logger.console(Category, [target, propertyKey, descriptor]);
    return descriptor;
  };
}

export class Logger {
  protected logger: Logger;

  public constructor(Obj: any) {
    if (!environment.production) {
      if (Obj) {
        this.logger = Log.create(Obj.constructor.name) as any;
      }
    }
  }

  public debug(Text: string, ...Obj: any[]) {
    if (this.logger) {
      this.logger.info(Text, ...Obj);
    }
  }

  public warn(Text: string, ...Obj: any[]) {
    if (this.logger) {
      this.logger.warn(Text, ...Obj);
    }
  }

  public error(Text: string, ...Obj: any[]) {
    if (this.logger) {
      this.logger.error(Text, ...Obj);
    }
  }

  public info(Text: string, ...Obj: any[]) {
    if (this.logger) {
      this.logger.info(Text, ...Obj);
    }
  }

  public static get console() {
    return Log.create('System').info;
  }

  public static get sprintf() {
    return sprintf;
  }
}



