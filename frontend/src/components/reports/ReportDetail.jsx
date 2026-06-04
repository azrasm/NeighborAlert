import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import api from '../../api/apiClient';
import { getStatusName, getCategoryName, getStatusChip } from '../../utils/helpers';
import { MiniMap } from '../maps/MiniMap';
import { FlagReportModal } from './FlagReportModal';
import Spinner from '../common/Spinner';
import Alert from '../common/Alert';

export const ReportDetail = ({ report, onBack, currentUser }) => {
  const [comments, setComments] = useState([]);
  const [commentsLoading, setCommentsLoading] = useState(true);
  const [newComment, setNewComment] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [showFlag, setShowFlag] = useState(false);
  const [flagged, setFlagged] = useState(() => localStorage.getItem(`flagged_${report.id}`) === "1");
  const [lightboxImg, setLightboxImg] = useState(null);
  const { authData } = useAuth();
  const upvoteKey = `upvoted_${authData?.username}_${report.id}`;
  const [upvoted, setUpvoted] = useState(() => localStorage.getItem(upvoteKey) === "1");
  const [upvoteCount, setUpvoteCount] = useState(report.upvoteCount || 0);
  const [upvoting, setUpvoting] = useState(false);

  useEffect(() => {
    (async () => {
      try { setComments(await api.getCommentsByReport(report.id)); }
      catch { setComments([]); }
      finally { setCommentsLoading(false); }
    })();
  }, [report.id]);

  const postComment = async () => {
    if (!newComment.trim()) return;
    setSubmitting(true); setError("");
    try {
      const c = await api.addComment({ reportId: report.id, userId: currentUser?.userId || 1, text: newComment });
      setComments(prev => [...prev, c]); setNewComment("");
    } catch (e) { setError(e.message); }
    finally { setSubmitting(false); }
  };

  const handleUpvote = async () => {
    if (upvoted || upvoting) return;
    setUpvoting(true);
    try { await api.upvoteReport({ reportId: report.id, userId: currentUser?.userId || 1 }); }
    catch {}
    finally {
      localStorage.setItem(upvoteKey, "1");
      setUpvoted(true);
      setUpvoteCount(c => c + 1);
      setUpvoting(false);
    }
  };

  const handleFlagged = () => {
    localStorage.setItem(`flagged_${report.id}`, "1");
    setFlagged(true);
  };

  return (
    <div>
      {lightboxImg && (
        <div className="lightbox" onClick={() => setLightboxImg(null)}>
          <img src={lightboxImg} alt="Uvećana fotografija" />
        </div>
      )}
      <div style={{ marginBottom: 20, display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <button className="btn btn-ghost btn-sm" onClick={onBack}>← Nazad</button>
        <button
          className={`flag-btn${flagged ? " flagged" : ""}`}
          onClick={() => !flagged && setShowFlag(true)}
          title="Prijavi lažni ili neprikladni sadržaj"
        >
          🚩 {flagged ? "Prijavljeno" : "Prijavi sadržaj"}
        </button>
      </div>

      <div className="detail-wrap">
        <div>
          <div className="detail-card" style={{ marginBottom: 16 }}>
            {report.mediaUrls && report.mediaUrls.length > 0 && (
              <div className="report-photos-grid" style={{ marginBottom: 16 }}>
                {report.mediaUrls.map((url, i) => (
                  <img key={i} src={url} alt={`Fotografija ${i+1}`} onClick={() => setLightboxImg(url)} title="Kliknite za uvećanje" />
                ))}
              </div>
            )}
            <div className="detail-title">{report.title}</div>
            <div className="report-meta" style={{ marginBottom: 14 }}>
              <span className={`chip ${getStatusChip(report)}`}>{getStatusName(report)}</span>
              <span className="chip chip-category">{getCategoryName(report)}</span>
            </div>
            <div className="detail-body">{report.description}</div>
            <div className="detail-kv">
              <div className="kv-item"><label>Adresa</label><p>📍 {report.address}</p></div>
              <div className="kv-item"><label>ID Korisnika</label><p>#{report.userId}</p></div>
              {report.latitude && <div className="kv-item"><label>Koordinate</label><p style={{ fontSize: 12 }}>🗺️ {report.latitude.toFixed(4)}, {report.longitude.toFixed(4)}</p></div>}
            </div>

            {report.latitude && report.longitude && (
              <div style={{ marginBottom: 16 }}>
                <div style={{ fontSize: 13, fontWeight: 600, color: "var(--text-secondary)", marginBottom: 8 }}>📍 Lokacija problema</div>
                <MiniMap lat={report.latitude} lng={report.longitude} />
              </div>
            )}

            <div style={{ paddingTop: 16, borderTop: "1px solid var(--border)", display: "flex", alignItems: "center", gap: 12 }}>
              <button
                className="btn btn-sm"
                onClick={handleUpvote}
                disabled={upvoted || upvoting}
                style={{ background: upvoted ? "rgba(46,213,115,0.15)" : "rgba(255,107,43,0.15)", color: upvoted ? "var(--green)" : "var(--accent-light)", border: `1px solid ${upvoted ? "rgba(46,213,115,0.3)" : "rgba(255,107,43,0.3)"}` }}
              >
                {upvoting ? <Spinner /> : upvoted ? "✅ Označili ste" : "👍 I mene pogađa"}
              </button>
              <span style={{ fontSize: 13, color: "var(--muted)" }}>
                {upvoteCount > 0 ? `${upvoteCount} ${upvoteCount === 1 ? "korisnik ima" : "korisnika ima"} isti problem` : "Budite prvi koji će označiti ovaj problem"}
              </span>
            </div>
          </div>

          <div className="detail-card">
            <div style={{ fontFamily: "var(--font-display)", fontWeight: 700, marginBottom: 14 }}>Komentari ({comments.length})</div>
            {error && <Alert>{error}</Alert>}
            <div style={{ display: "flex", gap: 8 }}>
              <input className="form-input" style={{ flex: 1 }} value={newComment} onChange={e => setNewComment(e.target.value)} placeholder="Dodaj komentar..." onKeyDown={e => e.key === "Enter" && postComment()} />
              <button className="btn btn-primary btn-sm" onClick={postComment} disabled={submitting}>{submitting ? <Spinner /> : "Pošalji"}</button>
            </div>
            {commentsLoading ? <div style={{ textAlign: "center", padding: 20 }}><Spinner /></div> :
              comments.length === 0 ? <div className="empty" style={{ padding: "24px 0" }}><div className="empty-text">Nema komentara</div></div> :
              <div className="comment-list">
                {comments.map((c, i) => (
                  <div key={c.id || i} className="comment-item">
                    <div className="comment-meta">Korisnik #{c.userId} · {c.createdAt ? new Date(c.createdAt).toLocaleDateString("bs") : "upravo"}</div>
                    <div className="comment-text">{c.text || c.content}</div>
                  </div>
                ))}
              </div>
            }
          </div>
        </div>

        <div>
          <div className="detail-card">
            <div style={{ fontFamily: "var(--font-display)", fontWeight: 700, marginBottom: 14, fontSize: 14 }}>Detalji prijave</div>
            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              {[["ID", `#${report.id}`], ["Naslov", report.title], ["Adresa", report.address],
                ["Kategorija", getCategoryName(report)],
                ["Status", getStatusName(report)],
                ["Korisnik", `#${report.userId}`]].map(([l, v]) => (
                <div key={l} style={{ display: "flex", justifyContent: "space-between", fontSize: 13, paddingBottom: 8, borderBottom: "1px solid var(--border)" }}>
                  <span style={{ color: "var(--muted)" }}>{l}</span>
                  <span style={{ fontWeight: 500 }}>{v}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {showFlag && (
        <FlagReportModal
          report={report}
          currentUser={currentUser}
          onClose={() => setShowFlag(false)}
          onFlagged={handleFlagged}
        />
      )}
    </div>
  );
};
export default ReportDetail;