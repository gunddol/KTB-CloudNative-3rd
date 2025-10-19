const Auth = (() => {
  const KEY = 'auth.user';
  function get(){ try { return JSON.parse(localStorage.getItem(KEY) || '{}'); } catch { return {}; } }
  function set(o){ localStorage.setItem(KEY, JSON.stringify(o || {})); }
  function clear(){ localStorage.removeItem(KEY); }
  function isLoggedIn(){ return !!get().userId; }
  function userId(){ return get().userId || null; }
  function profile(){ return get().profile || {}; }
  function setUserId(id){ const cur=get(); cur.userId=Number(id); set(cur); }
  function setProfile(p){ const cur=get(); cur.profile={...(cur.profile||{}), ...p}; set(cur); }
  return { get, set, clear, isLoggedIn, userId, profile, setUserId, setProfile };
})();
