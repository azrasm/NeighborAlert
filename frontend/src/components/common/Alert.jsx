export const Alert = ({ type = "error", children }) => {
  return <div className={`alert alert-${type}`}>{children}</div>;
};
export default Alert;