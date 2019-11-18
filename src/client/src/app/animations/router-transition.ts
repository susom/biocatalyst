import { animate, group, query, state, style, transition, trigger } from '@angular/animations';

export const RouterTransition = trigger('RouterTransition', [
  transition('* <=> *', [
    query(
      ':enter, :leave',
      style({position: 'fixed', width: '100%'}),
      {optional: true}
    ),
    group([  // block executes in parallel
      query(
        ':enter',
        [
          style({opacity: 0}),
          animate('.5s ease', style({opacity: 1}))
        ],
        {optional: true}
      ),
      query(
        ':leave',
        [
          style({opacity: 1}),
          animate('.5s ease-in-out', style({opacity: 0}))
        ],
        {optional: true})
    ])
  ])
]);

export const Fade = trigger('customFade', [
  state('in', style({opacity: 1})),
  // fade in when created. this could also be written as transition('void => *')
  transition(':enter', [
    style({opacity: 0, position: 'absolute'}),
    animate(100)
  ]),
  transition(':leave', [
    style({opacity: 1, position: 'absolute'}),
    animate(100)
  ])
]);
