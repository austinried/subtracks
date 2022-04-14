import React, { ComponentType, FunctionComponent, Suspense, SuspenseProps } from 'react'

export function withSuspense<P extends string | number | object>(
  WrappedComponent: ComponentType<P>,
  fallback: SuspenseProps['fallback'] = null,
): FunctionComponent<P> {
  function ComponentWithSuspense(props: P) {
    return (
      <Suspense fallback={fallback}>
        <WrappedComponent {...props} />
      </Suspense>
    )
  }

  return ComponentWithSuspense
}

export function withSuspenseMemo<P extends string | number | object>(
  WrappedComponent: ComponentType<P>,
  fallback: SuspenseProps['fallback'] = null,
  propsAreEqual?: Parameters<typeof React.memo>['1'],
) {
  function ComponentWithSuspense(props: P) {
    return (
      <Suspense fallback={fallback}>
        <WrappedComponent {...props} />
      </Suspense>
    )
  }

  return React.memo(ComponentWithSuspense, propsAreEqual)
}
