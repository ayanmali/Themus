import { useEffect } from "react";

export default function GithubCallback() {
//   useEffect(() => {
//     const code = new URLSearchParams(window.location.search).get("code");
//     console.log(code);
//   }, []);
  const code = new URLSearchParams(window.location.search).get("code");
  console.log(code);
  
  
  return (
    <div>
      GithubCallback
    </div>
  );
}