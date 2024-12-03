export function UsersIcon(props: React.SVGProps<SVGSVGElement>) {
  return (
    <svg
      {...props}
      xmlns="http://www.w3.org/2000/svg"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
      <path d="M16 3.13a4 4 0 0 1 0 7.75" />
    </svg>
  );
}

export function SettingsIcon(props: React.SVGProps<SVGSVGElement>) {
  return (
    <svg
      {...props}
      xmlns="http://www.w3.org/2000/svg"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );
}

export function SearchIcon(props: React.SVGProps<SVGSVGElement>) {
  return (
    <svg
      {...props}
      xmlns="http://www.w3.org/2000/svg"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <circle cx="11" cy="11" r="8" />
      <path d="m21 21-4.3-4.3" />
    </svg>
  );
}

export function Spinner() {
  return (
    <div className="absolute right-0 top-0 bottom-0 flex items-center justify-center">
      <svg
        className="animate-spin -ml-1 mr-3 h-5 w-5 text-gray-700"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
      >
        <circle
          className="opacity-25"
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          strokeWidth="4"
        />
        <path
          className="opacity-75"
          fill="currentColor"
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
        />
      </svg>
    </div>
  );
}

// export function Logo() {
//   return (
//     <svg
//       width="32"
//       height="32"
//       viewBox="0 0 32 32"
//       fill="none"
//       className="text-gray-100"
//       xmlns="http://www.w3.org/2000/svg"
//     >
//       <rect width="100%" height="100%" rx="16" fill="currentColor" />
//       <path
//         fillRule="evenodd"
//         clipRule="evenodd"
//         d="M17.6482 10.1305L15.8785 7.02583L7.02979 22.5499H10.5278L17.6482 10.1305ZM19.8798 14.0457L18.11 17.1983L19.394 19.4511H16.8453L15.1056 22.5499H24.7272L19.8798 14.0457Z"
//         fill="black"
//       />
//     </svg>
//   );
// }

export function Logo() {
  return (
    <svg
      width="1080"
      height="1080"
      viewBox="0 0 1080 1080"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <rect width="1080" height="1080" rx="540" fill="#0062F7" />
      <path
        d="M742.093 749.092L742.111 749.043L742.126 748.993L822.793 489.515C822.796 489.506 822.799 489.498 822.801 489.49C831.176 464.629 816.131 438.307 791.517 429.999L791.507 429.996C766.549 421.711 740.214 436.748 731.903 461.375L731.887 461.42L731.874 461.467L690.764 605.968L585.266 328.382L585.243 328.322L585.217 328.264C577.004 310.196 558.928 298.651 540.636 298.651C520.827 298.651 504.252 310.23 496.055 328.264L496.029 328.322L496.006 328.382L390.458 606.098L308.21 331.678L308.201 331.649L308.191 331.62C299.98 306.987 273.54 291.947 248.679 300.335C223.95 308.546 209.018 334.877 217.29 359.728C217.292 359.734 217.294 359.74 217.296 359.746L339.234 748.962C344.249 767.276 362.441 780.358 382.169 781.993L382.251 782H382.334H383.914C403.729 782 420.198 770.416 428.491 752.395L428.516 752.342L428.537 752.288L540.637 465.919L652.825 752.267C659.498 770.58 679.324 782 699.036 782H699.12L699.202 781.993C718.969 780.346 735.416 767.171 742.093 749.092Z"
        fill="white"
        stroke="white"
        stroke-width="4"
      />
      <path
        d="M817.737 393.313C844.498 393.313 866.158 371.653 866.158 344.893C866.158 319.751 844.437 298.151 817.737 298.151C791.025 298.151 770.897 319.862 770.897 344.893C770.897 371.541 790.966 393.313 817.737 393.313Z"
        fill="#FFBE17"
        stroke="#FFBE17"
        stroke-width="5"
      />
    </svg>
  );

}

// export function VercelLogo(props: React.SVGProps<SVGSVGElement>) {
//   return (
//     <svg
//       {...props}
//       aria-label="Vercel logomark"
//       height="64"
//       role="img"
//       viewBox="0 0 74 64"
//     >
//       <path
//         d="M37.5896 0.25L74.5396 64.25H0.639648L37.5896 0.25Z"
//         fill="currentColor"
//       ></path>
//     </svg>
//   );
// }
