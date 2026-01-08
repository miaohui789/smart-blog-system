export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

export function isValidUsername(str) {
  return str.length >= 3 && str.length <= 20
}

export function isValidEmail(email) {
  const reg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  return reg.test(email)
}

export function isValidPhone(phone) {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

export function isValidURL(url) {
  const reg = /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w .-]*)*\/?$/
  return reg.test(url)
}
