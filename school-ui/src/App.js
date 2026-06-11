import React, { useState, useEffect } from 'react';

function App({ keycloak }) {
  const [students, setStudents] = useState([]);
  const [userRole, setUserRole] = useState('');
  const [currentView, setCurrentView] = useState('dashboard'); 
  
  // --- Campus Wall States ---
  const [posts, setPosts] = useState([]);
  const [postInput, setPostInput] = useState("");
  const [commentInputs, setCommentInputs] = useState({});

  // --- Other States ---
  const [seats, setSeats] = useState([]);
  const [resources, setResources] = useState([]);
  const [resourceTitle, setResourceTitle] = useState('');
  const [resourceSubject, setResourceSubject] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [searchSubject, setSearchSubject] = useState('');
  const [rolePermissions, setRolePermissions] = useState({
    TEACHER: { canCreate: true, canUpdate: true, canDelete: false },
    STUDENT: { canCreate: false, canUpdate: false, canDelete: false }
  });
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [selectedStudentId, setSelectedStudentId] = useState(null);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [rollNo, setRollNo] = useState('');

  const activeRole = (userRole || "").toUpperCase();
  const currentUserName = keycloak?.tokenParsed?.preferred_username || "User";

  // ==========================================
  // --- CAMPUS WALL LOGIC ---
  // ==========================================
  const fetchPosts = async (token) => {
    try {
      const response = await fetch('http://localhost:8080/api/posts/all', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        const data = await response.json();
        setPosts(data.sort((a, b) => b.id - a.id)); 
      }
    } catch (error) {
      console.error("Error fetching posts:", error);
    }
  };

  const handleCreatePost = async () => {
    if (!postInput.trim()) return;
    const postPayload = { author: currentUserName, content: postInput, title: "Announcement" };
    try {
      const response = await fetch('http://localhost:8080/api/posts/add', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${keycloak.token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(postPayload)
      });
      if (response.ok) {
        setPostInput("");
        fetchPosts(keycloak.token);
      }
    } catch (error) {
      console.error("Post creation failed:", error);
    }
  };

  const handleReaction = async (postId, type) => {
    try {
      const response = await fetch(`http://localhost:8080/api/posts/${postId}/${type}`, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${keycloak.token}` }
      });
      if (response.ok) fetchPosts(keycloak.token);
    } catch (error) {
      console.error("Reaction failed:", error);
    }
  };

  const handleAddComment = async (postId) => {
    const text = commentInputs[postId];
    if (!text || !text.trim()) return;

    const commentPayload = { text: text, author: currentUserName };
    try {
      const response = await fetch(`http://localhost:8080/api/posts/${postId}/comment`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${keycloak.token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(commentPayload)
      });
      if (response.ok) {
        setCommentInputs({ ...commentInputs, [postId]: "" });
        fetchPosts(keycloak.token);
      }
    } catch (error) {
      console.error("Comment failed:", error);
    }
  };

  const handleDeletePost = async (postId) => {
    if (!window.confirm("Are you sure you want to permanently delete this post?")) return;
    try {
      const response = await fetch(`http://localhost:8080/api/posts/delete/${postId}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${keycloak.token}` }
      });
      if (response.ok) {
        fetchPosts(keycloak.token);
      } else {
        alert("Failed to delete the post. Check your role permissions.");
      }
    } catch (error) {
      console.error("Post deletion failed:", error);
    }
  };

  const handleResetReactions = async (postId) => {
    try {
      const response = await fetch(`http://localhost:8080/api/posts/${postId}/reset-reactions`, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${keycloak.token}` }
      });
      if (response.ok) {
        fetchPosts(keycloak.token);
      }
    } catch (error) {
      console.error("Resetting reactions failed:", error);
    }
  };

  // ==========================================
  // --- CORE SYSTEM EFFECTS ---
  // ==========================================
  useEffect(() => {
    if (keycloak) {
      const roles = keycloak.tokenParsed?.realm_access?.roles || [];
      const matchedRole = roles.includes('ADMIN') ? 'ADMIN' : roles.includes('TEACHER') ? 'TEACHER' : 'STUDENT';
      setUserRole(matchedRole);
      
      const token = keycloak.token;
      fetchStudents(token);
      fetchPermissions(token);
      fetchSeats(token);
      fetchResources(token);
      fetchPosts(token);
    }
  }, [keycloak]);

  // ==========================================
  // --- REGISTRY, LIBRARY, SEATS ACTIONS ---
  // ==========================================
  const fetchResources = async (token) => {
    if (!token) return;
    try {
      const response = await fetch('http://localhost:8080/api/resources/all', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) setResources(await response.json());
    } catch (error) { console.error("Library fetch error:", error); }
  };

  const handleSearchResources = async (e) => {
    e.preventDefault();
    if (!searchSubject.trim()) { fetchResources(keycloak.token); return; }
    try {
      const response = await fetch(`http://localhost:8080/api/resources/search?subject=${searchSubject}`, {
        headers: { 'Authorization': `Bearer ${keycloak.token}` }
      });
      if (response.ok) setResources(await response.json());
    } catch (error) { console.error("Search error:", error); }
  };

  const handleFileUpload = async (e) => {
    e.preventDefault();
    const formData = new FormData();
    formData.append('title', resourceTitle);
    formData.append('subject', resourceSubject);
    formData.append('file', selectedFile);
    try {
      const response = await fetch('http://localhost:8080/api/resources/upload', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${keycloak.token}` },
        body: formData
      });
      if (response.ok) {
        alert("Published!");
        setResourceTitle(''); setResourceSubject('');
        fetchResources(keycloak.token);
      }
    } catch (error) { console.error("Upload error:", error); }
  };

  const handleDownloadFile = (id, fileName) => {
    fetch(`http://localhost:8080/api/resources/download/${id}`, {
      headers: { 'Authorization': `Bearer ${keycloak.token}` }
    })
    .then(res => res.blob())
    .then(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = fileName || 'file';
      a.click();
    });
  };

  const fetchSeats = async (token) => {
    try {
      const response = await fetch('http://localhost:8080/api/seats/all', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) setSeats(await response.json());
    } catch (error) { console.error("Seats error:", error); }
  };

  const handleSeatAction = async (seatNum, currentStatus) => {
    const isBooked = (currentStatus || '').toUpperCase() === 'BOOKED';
    const path = isBooked ? `unbook/${seatNum}` : `book/${seatNum}`;
    try {
      const response = await fetch(`http://localhost:8080/api/seats/${path}`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${keycloak.token}` }
      });
      if (response.ok) fetchSeats(keycloak.token);
    } catch (error) { console.error("Seat action error:", error); }
  };

  const fetchStudents = async (token) => {
    try {
      const response = await fetch('http://localhost:8080/api/students/all', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) setStudents(await response.json());
    } catch (error) { console.error("Student fetch error:", error); }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const payload = { name, email, studentRollNumber: rollNo };
    const url = isEditing ? `http://localhost:8080/api/students/update/${selectedStudentId}` : 'http://localhost:8080/api/students/add';
    try {
      const response = await fetch(url, {
        method: isEditing ? 'PUT' : 'POST',
        headers: { 'Authorization': `Bearer ${keycloak.token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (response.ok) { resetForm(); fetchStudents(keycloak.token); }
    } catch (error) { console.error("Submit error:", error); }
  };

  const handleDelete = async (student) => {
    if (!window.confirm("Delete?")) return;
    try {
      const response = await fetch(`http://localhost:8080/api/students/delete/${student.id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${keycloak.token}` }
      });
      if (response.ok) fetchStudents(keycloak.token);
    } catch (error) { console.error("Delete error:", error); }
  };

  const fetchPermissions = async (token) => {
    try {
      const response = await fetch('http://localhost:8080/api/permissions', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        const data = await response.json();
        setRolePermissions(data || {});
      }
    } catch (e) { console.warn("Permissions API offline."); }
  };

  const handleSavePermissions = async () => {
    try {
      await fetch('http://localhost:8080/api/permissions/update', {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${keycloak.token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify(rolePermissions)
      });
      alert("Saved!");
    } catch (e) { console.error("Save error:", e); }
  };

  const handlePermissionCheckboxChange = (role, type) => {
    setRolePermissions(prev => ({
      ...prev,
      [role]: { 
        ...(prev?.[role] || {}), 
        [type]: !prev?.[role]?.[type] 
      }
    }));
  };

  const activePermissions = {
    canCreate: activeRole === "ADMIN" || rolePermissions?.[activeRole]?.canCreate || false,
    canUpdate: activeRole === "ADMIN" || rolePermissions?.[activeRole]?.canUpdate || false,
    canDelete: activeRole === "ADMIN" || rolePermissions?.[activeRole]?.canDelete || false
  };

  const resetForm = () => {
    setIsFormOpen(false); setIsEditing(false); setName(''); setEmail(''); setRollNo('');
  };

  const handleEditClick = (s) => {
    setIsEditing(true); setSelectedStudentId(s.id);
    setName(s.name); setEmail(s.email); setRollNo(s.studentRollNumber || '');
    setIsFormOpen(true);
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif', backgroundColor: '#fdfefe', minHeight: '100vh' }}>
      
      {/* NAVIGATION BAR */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '15px', borderBottom: '2px solid #eaeded', marginBottom: '20px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '35px' }}>
          <h2 style={{ margin: 0, color: '#2c3e50' }}>🏫 School Management Hub</h2>
          <div style={{ display: 'flex', gap: '10px' }}>
            <button onClick={() => setCurrentView('dashboard')} style={{ padding: '10px 20px', cursor: 'pointer', borderRadius: '6px', border: 'none', backgroundColor: currentView === 'dashboard' ? '#34495e' : '#ebf5fb', color: currentView === 'dashboard' ? 'white' : '#2980b9', fontWeight: 'bold' }}>📊 Registry</button>
            <button onClick={() => setCurrentView('wall')} style={{ padding: '10px 20px', cursor: 'pointer', borderRadius: '6px', border: 'none', backgroundColor: currentView === 'wall' ? '#8e44ad' : '#f5eef8', color: currentView === 'wall' ? 'white' : '#8e44ad', fontWeight: 'bold' }}>📱 Campus Wall</button>
            <button onClick={() => setCurrentView('library')} style={{ padding: '10px 20px', cursor: 'pointer', borderRadius: '6px', border: 'none', backgroundColor: currentView === 'library' ? '#b7950b' : '#fef9e7', color: currentView === 'library' ? 'white' : '#b7950b', fontWeight: 'bold' }}>📚 Library</button>
          </div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
          <p style={{ margin: 0 }}>Active: <strong style={{ color: '#2980b9' }}>{currentUserName}</strong> ({activeRole})</p>
          <button onClick={() => keycloak.logout()} style={{ padding: '8px 18px', cursor: 'pointer', borderRadius: '4px', border: 'none', backgroundColor: '#bdc3c7', fontWeight: 'bold' }}>Logout</button>
        </div>
      </div>

      {/* DASHBOARD VIEW */}
      {currentView === 'dashboard' && (
        <div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <h3>Student Enrollment Registry</h3>
            {activePermissions.canCreate && <button onClick={() => setIsFormOpen(true)} style={{ backgroundColor: '#2ecc71', color: 'white', padding: '10px 20px', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>+ Add Student</button>}
          </div>

          {activeRole === 'ADMIN' && (
            <div style={{ border: '2px solid #2980b9', padding: '20px', marginBottom: '20px', backgroundColor: '#ebf5fb', borderRadius: '8px' }}>
              <h4>Role Privileges Control</h4>
              <table style={{ width: '100%', backgroundColor: 'white', borderCollapse: 'collapse', marginBottom: '15px' }}>
                <thead><tr style={{ backgroundColor: '#34495e', color: 'white' }}><th style={{ padding: '10px' }}>Role</th><th style={{ padding: '10px' }}>Create</th><th style={{ padding: '10px' }}>Update</th><th style={{ padding: '10px' }}>Delete</th></tr></thead>
                <tbody>
                  {['TEACHER', 'STUDENT'].map(role => {
                    const currentRoleData = rolePermissions?.[role] || { canCreate: false, canUpdate: false, canDelete: false };
                    return (
                      <tr key={role} style={{ borderBottom: '1px solid #ddd' }}>
                        <td style={{ padding: '10px', fontWeight: 'bold' }}>{role}</td>
                        {['canCreate', 'canUpdate', 'canDelete'].map(p => (
                          <td key={p} style={{ textAlign: 'center', padding: '10px' }}>
                            <input 
                              type="checkbox" 
                              checked={currentRoleData[p] || false} 
                              onChange={() => handlePermissionCheckboxChange(role, p)} 
                            />
                          </td>
                        ))}
                      </tr>
                    );
                  })}
                </tbody>
              </table>
              <button onClick={handleSavePermissions} style={{ backgroundColor: '#2980b9', color: 'white', border: 'none', padding: '8px 16px', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>Save DB</button>
            </div>
          )}

          {isFormOpen && (
            <form onSubmit={handleSubmit} style={{ display: 'flex', gap: '10px', marginBottom: '20px', background: '#eee', padding: '15px', borderRadius: '6px' }}>
              <input value={name} onChange={e => setName(e.target.value)} placeholder="Name" required style={{ padding: '6px' }} />
              <input value={email} onChange={e => setEmail(e.target.value)} placeholder="Email" required style={{ padding: '6px' }} />
              <input value={rollNo} onChange={e => setRollNo(e.target.value)} placeholder="Roll No" required style={{ padding: '6px' }} />
              <button type="submit" style={{ backgroundColor: '#2ecc71', color: 'white', border: 'none', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer' }}>Save</button>
              <button type="button" onClick={resetForm} style={{ backgroundColor: '#95a5a6', color: 'white', border: 'none', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer' }}>Cancel</button>
            </form>
          )}

          <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '30px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
            <thead>
              <tr style={{ backgroundColor: '#2c3e50', color: 'white', textAlign: 'left' }}>
                <th style={{ padding: '12px', border: '1px solid #ddd' }}>Name</th>
                <th style={{ padding: '12px', border: '1px solid #ddd' }}>Email</th>
                <th style={{ padding: '12px', border: '1px solid #ddd' }}>Roll No</th>
                <th style={{ padding: '12px', border: '1px solid #ddd' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {(!students || students.length === 0) ? (
                <tr>
                  <td colSpan="4" style={{ padding: '20px', textAlign: 'center', color: '#7f8c8d', backgroundColor: 'white' }}>No students registered in the system database.</td>
                </tr>
              ) : (
                students.map(s => (
                  <tr key={s.id} style={{ backgroundColor: 'white', borderBottom: '1px solid #ddd' }}>
                    <td style={{ padding: '12px', border: '1px solid #ddd', color: '#34495e' }}>{s.name}</td>
                    <td style={{ padding: '12px', border: '1px solid #ddd', color: '#34495e' }}>{s.email}</td>
                    <td style={{ padding: '12px', border: '1px solid #ddd', color: '#34495e' }}>{s.studentRollNumber}</td>
                    <td style={{ padding: '12px', border: '1px solid #ddd', gap: '8px', display: 'flex' }}>
                      <button 
                        disabled={!activePermissions.canUpdate} 
                        onClick={() => handleEditClick(s)}
                        style={{ padding: '4px 10px', cursor: 'pointer', backgroundColor: '#f39c12', color: 'white', border: 'none', borderRadius: '4px' }}
                      >
                        Edit
                      </button>
                      <button 
                        disabled={!activePermissions.canDelete} 
                        onClick={() => handleDelete(s)}
                        style={{ padding: '4px 10px', cursor: 'pointer', backgroundColor: '#e74c3c', color: 'white', border: 'none', borderRadius: '4px' }}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>

          <div style={{ padding: '20px', backgroundColor: '#e8f8f5', borderRadius: '8px' }}>
            <h3>🏫 Seat Reservation</h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(100px, 1fr))', gap: '10px' }}>
              {(seats || []).map(seat => (
                <button key={seat.id} onClick={() => handleSeatAction(seat.seatNumber, seat.status)} style={{ padding: '10px', backgroundColor: seat.status === 'AVAILABLE' ? '#2ecc71' : '#e74c3c', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer' }}>
                  {seat.seatNumber} <br/> <small>{seat.status}</small>
                </button>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* CAMPUS WALL VIEW */}
      {currentView === 'wall' && (
        <div style={{ maxWidth: '700px', margin: '0 auto' }}>
          <h3 style={{ color: '#8e44ad', textAlign: 'center' }}>📱 Campus Wall</h3>
          {activeRole === 'ADMIN' && (
            <div style={{ background: 'white', padding: '15px', borderRadius: '8px', border: '1px solid #ddd', marginBottom: '20px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
              <textarea 
                style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #eee', outline: 'none', resize: 'none' }} 
                rows="3"
                placeholder="What's the latest announcement, Admin?" 
                value={postInput} 
                onChange={e => setPostInput(e.target.value)} 
              />
              <div style={{ textAlign: 'right', marginTop: '10px' }}>
                <button onClick={handleCreatePost} style={{ backgroundColor: '#8e44ad', color: 'white', border: 'none', padding: '8px 24px', borderRadius: '20px', cursor: 'pointer', fontWeight: 'bold' }}>Post Announcement</button>
              </div>
            </div>
          )}

          {(posts || []).map(post => (
            <div key={post.id} style={{ background: 'white', borderRadius: '8px', border: '1px solid #ddd', marginBottom: '20px', overflow: 'hidden', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
              
              {/* Header with conditional Delete Button */}
              <div style={{ padding: '12px 15px', background: '#f8f9fa', borderBottom: '1px solid #eee', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <div style={{ width: '35px', height: '35px', borderRadius: '50%', backgroundColor: '#8e44ad', color: 'white', display: 'flex', justifyContent: 'center', alignItems: 'center', fontWeight: 'bold' }}>
                    {post.author?.[0]?.toUpperCase() || 'U'}
                  </div>
                  <strong>{post.author || 'Unknown'}</strong>
                </div>
                {activeRole === 'ADMIN' && (
                  <button 
                    onClick={() => handleDeletePost(post.id)} 
                    style={{ background: '#e74c3c', color: 'white', border: 'none', padding: '5px 10px', borderRadius: '4px', cursor: 'pointer', fontSize: '12px', fontWeight: 'bold' }}
                  >
                    🗑️ Delete Post
                  </button>
                )}
              </div>

              <div style={{ padding: '20px', fontSize: '16px', color: '#2c3e50', lineHeight: '1.5' }}>
                {post.content}
              </div>

              {/* Reaction Bar with conditional Reset Button */}
              <div style={{ padding: '10px 15px', borderTop: '1px solid #f0f0f0', display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: '#7f8c8d' }}>
                <div style={{ display: 'flex', gap: '20px' }}>
                  <button onClick={() => handleReaction(post.id, 'like')} style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '5px', fontSize: '15px' }}>
                     <span>👍</span> {post.likes || 0}
                  </button>
                  <button onClick={() => handleReaction(post.id, 'dislike')} style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '5px', fontSize: '15px' }}>
                     <span>👎</span> {post.dislikes || 0}
                  </button>
                </div>
                {activeRole === 'ADMIN' && (
                  <button 
                    onClick={() => handleResetReactions(post.id)} 
                    style={{ background: 'none', border: 'none', color: '#7f8c8d', cursor: 'pointer', fontSize: '13px', textDecoration: 'underline' }}
                  >
                    🔄 Reset Counters
                  </button>
                )}
              </div>

              <div style={{ background: '#fcfcfc', borderTop: '1px solid #f0f0f0', padding: '15px' }}>
                <div style={{ marginBottom: '10px' }}>
                  {post.comments?.map((comment, idx) => (
                    <div key={idx} style={{ background: '#f1f2f6', padding: '8px 12px', borderRadius: '15px', marginBottom: '8px', display: 'inline-block', maxWidth: '90%' }}>
                      <strong style={{ fontSize: '12px', color: '#2c3e50', display: 'block' }}>{comment.author}</strong>
                      <span style={{ fontSize: '14px' }}>{comment.text}</span>
                    </div>
                  ))}
                </div>
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                  <input 
                    style={{ flex: 1, padding: '8px 15px', borderRadius: '20px', border: '1px solid #ddd', outline: 'none' }} 
                    placeholder="Write a comment..." 
                    value={commentInputs[post.id] || ""} 
                    onChange={e => setCommentInputs({...commentInputs, [post.id]: e.target.value})}
                    onKeyDown={(e) => e.key === 'Enter' && handleAddComment(post.id)}
                  />
                  <button onClick={() => handleAddComment(post.id)} style={{ background: '#8e44ad', color: 'white', border: 'none', padding: '6px 12px', borderRadius: '12px', cursor: 'pointer', fontSize: '13px' }}>Send</button>
                </div>
              </div>

            </div>
          ))}
        </div>
      )}

      {/* LIBRARY VIEW */}
      {currentView === 'library' && (
        <div style={{ padding: '25px', backgroundColor: '#fffdf3', border: '1px solid #f9e79f', borderRadius: '12px' }}>
          <h3>📚 Digital Library</h3>
          <form onSubmit={handleSearchResources} style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
            <input style={{ flex: 1 }} placeholder="Search Subject..." value={searchSubject} onChange={e => setSearchSubject(e.target.value)} />
            <button type="submit">Search</button>
          </form>
          {(activeRole === 'ADMIN' || activeRole === 'TEACHER') && (
            <form onSubmit={handleFileUpload} style={{ marginBottom: '20px', display: 'flex', gap: '10px' }}>
              <input placeholder="Title" value={resourceTitle} onChange={e => setResourceTitle(e.target.value)} required />
              <input placeholder="Subject" value={resourceSubject} onChange={e => setResourceSubject(e.target.value)} required />
              <input type="file" onChange={e => setSelectedFile(e.target.files[0])} required />
              <button type="submit">Upload</button>
            </form>
          )}
          <table style={{ width: '100%', background: 'white' }}>
            <thead><tr style={{ background: '#f5b041', color: 'white' }}><th>Title</th><th>Subject</th><th>Action</th></tr></thead>
            <tbody>
              {(resources || []).map(res => (
                <tr key={res.id}>
                  <td>{res.title}</td><td>{res.subject}</td>
                  <td><button onClick={() => handleDownloadFile(res.id, res.title)}>Download</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default App;