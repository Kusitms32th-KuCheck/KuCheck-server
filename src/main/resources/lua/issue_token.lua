-- KEYS:
--   [1] memberKey    : "attendance:member:{memberId}:active"
--   [2] tokenPrefix  : "attendance:token:"
--   [3] newTokenKey  : "attendance:token:{newToken}"
--
-- ARGV:
--   [1] payload            : "memberId|issuedMs|expMs|usedFlag"
--   [2] ttlSeconds         : new token TTL (seconds)
--   [3] newToken           : new token string
--   [4] usedGraceSeconds   : optional, default 30 (seconds)

local memberKey        = KEYS[1]
local tokenPrefix      = KEYS[2]
local newTokenKey      = KEYS[3]
local payload          = ARGV[1]
local ttlSeconds       = tonumber(ARGV[2])
local newToken         = ARGV[3]
local usedGraceSeconds = tonumber(ARGV[4]) or 30
local capMs            = usedGraceSeconds * 1000

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

local oldToken = redis.call('GET', memberKey)
if oldToken then
  local oldTokenKey = tokenPrefix .. oldToken
  local oldVal = redis.call('GET', oldTokenKey)
  if oldVal then
    local parts = parse_payload(oldVal)
    if parts then
      local ttlms = redis.call('PTTL', oldTokenKey)
      if parts[4] ~= '1' then
        parts[4] = '1'
        local updatedVal = table.concat(parts, '|')
        redis.call('SET', oldTokenKey, updatedVal)
      end
      adjust_ttl(oldTokenKey, ttlms, capMs)
    end
  end
end

redis.call('SET', newTokenKey, payload, 'EX', ttlSeconds)
redis.call('SET', memberKey, newToken, 'EX', ttlSeconds)

return 'OK'
