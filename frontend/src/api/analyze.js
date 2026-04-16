import axios from 'axios'

export const analyzeUrl = async (url) => {
  const { data } = await axios.post('/api/analyze', { url })
  return data
}
