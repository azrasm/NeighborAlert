import { getStatusName, getCategoryName, getStatusChip } from '../../utils/helpers';

export const ReportCard = ({ report, onClick }) => {
  return (
    <div className="report-card" onClick={() => onClick(report)}>
      {report.mediaUrls && report.mediaUrls.length > 0 && (
        <img src={report.mediaUrls[0]} alt="" style={{ width: "100%", height: 140, objectFit: "cover", borderRadius: 8, marginBottom: 12 }} onClick={e => e.stopPropagation()} />
      )}
      <div className="report-title">{report.title}</div>
      <div className="report-addr">📍 {report.address}{report.latitude ? " 🗺️" : ""}</div>
      <div className="report-desc">{report.description}</div>
      <div className="report-meta">
        <span className={`chip ${getStatusChip(report)}`}>{getStatusName(report)}</span>
        <span className="chip chip-category">{getCategoryName(report)}</span>
        {report.upvoteCount > 0 && <span className="chip chip-category">👍 {report.upvoteCount}</span>}
      </div>
    </div>
  );
};
export default ReportCard;