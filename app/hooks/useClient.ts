import { useStore } from '@app/state/store'

const useClient = () => {
  const client = useStore(store => store.client)

  return () => {
    if (!client) {
      throw new Error('no client!')
    }

    return client
  }
}

export default useClient
