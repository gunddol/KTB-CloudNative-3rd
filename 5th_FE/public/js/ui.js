function $(sel, root=document){ return root.querySelector(sel); }
function renderNav() {
  const nav = document.createElement('div');
  nav.className = 'nav';
  nav.innerHTML = `
    <div class="brand"><a href="/">아무 말 대잔치</a></div>
    <div class="right">
      <a href="/posts/new" class="button secondary" id="newPost">새 글</a>
      <img class="profile-pic" id="navProfile" src="${Auth.profile().imageUrl || 'https://placekitten.com/64/64'}" title="프로필" />
    </div>`;
  document.body.prepend(nav);
  $('#navProfile').addEventListener('click', () => {
    if (!Auth.isLoggedIn()) location.href = '/login'; else location.href = '/profile';
  });
  $('#newPost').addEventListener('click', (e) => {
    if (!Auth.isLoggedIn()) { e.preventDefault(); location.href = '/login'; }
  });
}
function requireLogin(redirect='/login'){ if(!Auth.isLoggedIn()){ location.href = redirect; return false; } return true; }
