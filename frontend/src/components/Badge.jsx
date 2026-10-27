import React from "react";

export default function Badge({ value }) {
  return (
    <span
      className={`badge ${["DISPONIBLE", "DEVUELTO"].includes(value) ? "green" : "amber"}`}
    >
      {value.toLowerCase()}
    </span>
  );
}
