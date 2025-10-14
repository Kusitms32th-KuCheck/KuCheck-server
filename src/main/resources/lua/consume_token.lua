-- KEYS[1] = tokenKey ("attendance:token:{token}")
-- ARGV[1] = usedGraceSeconds (e.g., "30")

local tokenKey = KEYS[1]
local capMs    = (tonumber(ARGV[1]) or 30) * 1000

local function parse_payload(v)
  if not v then return nil end
  local parts = {}
  for p in string.gmatch(v, '([^|]+)') do table.insert(parts, p) end
  if #parts ~= 4 then return nil end
  return parts
end

local function adjust_ttl(key, ttlms, cap)
  if ttlms <= 0 or ttlms > cap then
    redis.call('PEXPIRE', key, cap)
  else
    redis.call('PEXPIRE', key, ttlms)
  end
end

local v = redis.call('GET', tokenKey)
if not v then return nil end

local parts = parse_payload(v)
if not parts then return nil end

local ttlms = redis.call('PTTL', tokenKey)

if parts[4] ~= '1' then
  parts[4] = '1'
  local updated = table.concat(parts, '|')
  redis.call('SET', tokenKey, updated)
end

adjust_ttl(tokenKey, ttlms, capMs)

return v
